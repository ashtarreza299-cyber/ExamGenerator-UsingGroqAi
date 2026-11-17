package org.example.service;

import org.example.config.ApiKeyManager;
import org.example.config.NetworkUtils;
import org.example.model.BlankSpaceQuestion;
import org.example.model.DescriptiveQuestion;
import org.example.model.ExamStructure;
import org.example.model.Model;
import org.example.model.ModelListener;
import org.example.model.MultipleChoiceQuestion;
import org.example.model.Question;
import org.example.model.TrueFalseQuestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.regex.*;

public class ExamGenerator {

    private static final String API_URL = "https://api.perplexity.ai/chat/completions";
    private static final String MODEL   = "perplexity-decider-flash-v1.3"; // Decider Flash — fast classification/generation model
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private static final String SYSTEM_PROMPT =
            "You are an expert exam analyzer. You receive preprocessed exam text.\n\n" +
                    "QUESTION TYPES:\n" +
                    "  MCQ         = Has options A) B) C) D) — options may contain [IMAGE] for image options\n" +
                    "  TF          = Has True/False as answer choices\n" +
                    "  BLANK       = Has blanks like '______' or '__________'\n" +
                    "  DESCRIPTIVE = Open ended: explain, describe, compare, write, discuss\n\n" +
                    "RULES:\n" +
                    "  - Extract EVERY question. DO NOT classify everything as DESCRIPTIVE.\n" +
                    "  - [IMAGE] means there is a picture at that position\n" +
                    "  - For MCQ: options array = text of each option (use '[IMAGE]' if option is image)\n" +
                    "  - For TF, BLANK, DESCRIPTIVE: options = []\n" +
                    "  - Ignore: university, faculty, date, name fields, part titles, marks, Good Luck\n" +
                    "  - Document may be Persian, English, Dari, or mixed\n\n" +
                    "Return ONLY a JSON array:\n" +
                    "[\n" +
                    "  {\n" +
                    "    \"partTitle\": \"Part One: MCQ\",\n" +
                    "    \"questions\": [\n" +
                    "      {\"stem\": \"What is Java?\", \"type\": \"MCQ\", \"options\": [\"A Database\", \"A Language\", \"A Browser\", \"An OS\"]},\n" +
                    "      {\"stem\": \"What is the default value? [IMAGE]\", \"type\": \"MCQ\", \"options\": [\"[IMAGE]\", \"[IMAGE]\", \"[IMAGE]\", \"[IMAGE]\"]},\n" +
                    "      {\"stem\": \"Java is platform independent.\", \"type\": \"TF\", \"options\": []},\n" +
                    "      {\"stem\": \"The ______ keyword creates an object.\", \"type\": \"BLANK\", \"options\": []},\n" +
                    "      {\"stem\": \"Explain polymorphism.\", \"type\": \"DESCRIPTIVE\", \"options\": []}\n" +
                    "    ]\n" +
                    "  }\n" +
                    "]";

    // ================================================================
    // Public entry point
    // ================================================================

    public static void generate(JPanel parent, Model model) {

        if (model.getOutputFolder() == null) {
            JOptionPane.showMessageDialog(parent, "Please select output folder!"); return;
        }
        if (model.getSelectedFile() == null) {
            JOptionPane.showMessageDialog(parent, "Please load a Word file first!"); return;
        }
        int studentCount = model.getNumberOfStudents();
        if (studentCount <= 0) {
            JOptionPane.showMessageDialog(parent, "Please set number of students!"); return;
        }
        String apiKey = ApiKeyManager.loadKey();
        if (apiKey == null) {
            JOptionPane.showMessageDialog(parent,
                    "Perplexity API key not found!\nPlease restart the app."); return;
        }

        JOptionPane.showMessageDialog(parent,
                "Analyzing exam structure...\nThis may take a few seconds.");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {

                // ── 1. Read original paragraphs ───────────────────
                List<ParaInfo> paras = readParagraphs(model.getSelectedFile());

                // ── 2. Preprocess → expand tabs, mark images ──────
                List<ParaInfo> processed = preprocess(paras);

                // ── 3. Build text for the model ────────────────────────
                StringBuilder sb = new StringBuilder();
                for (ParaInfo p : processed) {
                    if (!p.displayText.isBlank()) sb.append(p.displayText).append("\n");
                }

                System.out.println("=== Sending to Perplexity ===\n" + sb.toString().substring(0, Math.min(1000, sb.length())));

                // ── 4. Ask the model ───────────────────────────────────
                List<PartInfo> parts = analyzeWithPerplexity(sb.toString(), apiKey);
                if (parts == null || parts.isEmpty()) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(parent,
                                    "Could not analyze exam structure.\nPlease try again."));
                    return null;
                }

                System.out.println("Model found " + parts.size() + " parts, " +
                        parts.stream().mapToInt(p -> p.questions.size()).sum() + " questions");

                // ── 5. Match model text → original paragraph indices ──
                for (PartInfo part : parts) {
                    for (QuestionInfo q : part.questions) {
                        q.stemParaIdx = findParaIndex(processed, q.stemText, -1);
                        for (int oi = 0; oi < q.optionTexts.size(); oi++) {
                            String optText = q.optionTexts.get(oi);
                            int idx = "[IMAGE]".equals(optText)
                                    ? findImageOptionIndex(processed, q.stemParaIdx, oi)
                                    : findParaIndex(processed, optText, q.stemParaIdx);
                            q.optionParaIdxs.add(idx);
                        }
                    }
                }

                // ── 6. Collect all question original para indices ──
                Set<Integer> questionOrigIdx = new HashSet<>();
                for (PartInfo part : parts) {
                    for (QuestionInfo q : part.questions) {
                        addOrigIdx(questionOrigIdx, processed, q.stemParaIdx);
                        for (int idx : q.optionParaIdxs) addOrigIdx(questionOrigIdx, processed, idx);
                    }
                }

                // ── 7. Part start map (by original para index) ────
                Map<Integer, PartInfo> partByOrigStart = new LinkedHashMap<>();
                for (PartInfo part : parts) {
                    for (QuestionInfo q : part.questions) {
                        if (q.stemParaIdx >= 0) {
                            int origIdx = processed.get(q.stemParaIdx).origIndex;
                            partByOrigStart.put(origIdx, part);
                            break;
                        }
                    }
                }

                // ── 8. Generate output document ───────────────────
                try (XWPFDocument outputDoc = new XWPFDocument()) {

                    for (int s = 0; s < studentCount; s++) {

                        try (FileInputStream fis = new FileInputStream(model.getSelectedFile());
                             XWPFDocument srcDoc = new XWPFDocument(fis)) {

                            List<XWPFParagraph> allOrigParas = srcDoc.getParagraphs();

                            // Build per-student filtered+shuffled parts
                            Map<Integer, PartInfo> studentParts = buildStudentParts(parts, processed, model, partByOrigStart);

                            // Walk original paragraphs
                            int i = 0;
                            while (i < allOrigParas.size()) {

                                PartInfo partAtI = partByOrigStart.get(i);

                                if (partAtI != null) {
                                    // Find corresponding student part
                                    PartInfo studentPart = studentParts.get(i);
                                    // Find original part to know how far to skip
                                    int skipTo = getPartLastOrigIdx(partAtI, processed) + 1;

                                    if (studentPart != null && !studentPart.questions.isEmpty()) {
                                        int qNum = 1;
                                        for (QuestionInfo q : studentPart.questions) {
                                            writeQuestion(q, qNum, allOrigParas, processed,
                                                    srcDoc, outputDoc, model);
                                            qNum++;
                                            outputDoc.createParagraph(); // spacing
                                        }
                                    }

                                    i = skipTo;

                                } else if (questionOrigIdx.contains(i)) {
                                    i++; // skip — already written above
                                } else {
                                    // Non-question para — copy as-is (header, titles, etc.)
                                    copyParagraphWithImages(allOrigParas.get(i), outputDoc, -1);
                                    i++;
                                }
                            }

                            if (s < studentCount - 1) {
                                outputDoc.createParagraph().setPageBreak(true);
                            }
                        }
                    }

                    // Save
                    String fn = (model.getFileNaming() != null && !model.getFileNaming().isBlank())
                            ? model.getFileNaming() : "Exam";
                    File outFile = new File(model.getOutputFolder(),
                            fn + "_" + model.getStartNumber() + ".docx");
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        outputDoc.write(fos);
                    }

                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(parent,
                                    "✓ Exam generated!\n" + studentCount + " students → " +
                                            outFile.getAbsolutePath()));
                }

                return null;
            }

            @Override
            protected void done() {
                try { get(); } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(parent, "Error: " + ex.getMessage()));
                }
            }
        };

        worker.execute();
    }

    // ================================================================
    // Step 1: Read paragraphs from docx
    // ================================================================

    private static List<ParaInfo> readParagraphs(File file) throws Exception {
        List<ParaInfo> result = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {
            int i = 0;
            for (XWPFParagraph p : doc.getParagraphs()) {
                String text = p.getText() == null ? "" : p.getText();
                String xml  = p.getCTP().xmlText();
                boolean hasImg = xml.contains("w:drawing") || xml.contains("w:pict");
                result.add(new ParaInfo(i++, text, hasImg));
            }
        }
        return result;
    }

    // ================================================================
    // Step 2: Preprocess — expand inline options, mark images
    // ================================================================

    private static List<ParaInfo> preprocess(List<ParaInfo> paras) {
        List<ParaInfo> result = new ArrayList<>();

        // Pattern: "    A) text\t\tB) other" — two options on one line
        Pattern twoOpts = Pattern.compile(
                "(?i)^(\\s*[A-Da-d][.)\\]]\\s*.+?)\\t+([A-Da-d][.)\\]]\\s*.+)$"
        );

        for (ParaInfo p : paras) {
            String text = p.rawText;

            // Check for two options on one line (tab-separated)
            Matcher m = twoOpts.matcher(text);
            if (m.matches()) {
                // Split into two separate option lines
                String opt1 = m.group(1).trim();
                String opt2 = m.group(2).trim();

                ParaInfo p1 = new ParaInfo(p.origIndex, opt1, false);
                ParaInfo p2 = new ParaInfo(p.origIndex, opt2, false); // same origIndex — virtual

                // If the original para had images, mark them
                if (p.hasImage) {
                    p1.hasImage = true;
                    p1.displayText = buildDisplayText(opt1, true);
                    p2.displayText = buildDisplayText(opt2, false);
                } else {
                    p1.displayText = opt1;
                    p2.displayText = opt2;
                }

                result.add(p1);
                result.add(p2);
                continue;
            }

            // Build display text with [IMAGE] marker
            p.displayText = buildDisplayText(text, p.hasImage);
            result.add(p);
        }

        return result;
    }

    private static String buildDisplayText(String text, boolean hasImage) {
        if (hasImage && text.isBlank()) return "[IMAGE]";
        if (hasImage) return text + " [IMAGE]";
        return text;
    }

    // ================================================================
    // Step 5: Match question text to paragraph index
    // ================================================================

    private static int findParaIndex(List<ParaInfo> paras, String queryText, int afterIdx) {
        if (queryText == null || queryText.isBlank()) return -1;

        // Clean query: remove number prefix, [IMAGE], lowercase
        String clean = queryText
                .replaceFirst("^\\s*\\d+[.)\\-]\\s*", "")
                .replace("[IMAGE]", "").trim().toLowerCase();

        int start = Math.max(0, afterIdx);

        for (int i = start; i < paras.size(); i++) {
            String paraClean = paras.get(i).rawText
                    .replaceFirst("^\\s*\\d+[.)\\-]\\s*", "")
                    .replaceFirst("(?i)^\\s*[A-Da-d][.)\\-]\\s*", "")
                    .trim().toLowerCase();

            if (!clean.isEmpty() && !paraClean.isEmpty()) {
                if (paraClean.equals(clean)
                        || (clean.length() > 8 && paraClean.contains(clean))
                        || (paraClean.length() > 8 && clean.contains(paraClean))) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int findImageOptionIndex(List<ParaInfo> paras, int stemIdx, int optionOrder) {
        // Find the Nth image option after the stem
        int count = 0;
        int start = stemIdx >= 0 ? stemIdx + 1 : 0;
        for (int i = start; i < Math.min(paras.size(), start + 15); i++) {
            if (paras.get(i).hasImage || paras.get(i).displayText.contains("[IMAGE]")) {
                if (count == optionOrder) return i;
                count++;
            }
        }
        return -1;
    }

    private static void addOrigIdx(Set<Integer> set, List<ParaInfo> processed, int procIdx) {
        if (procIdx >= 0 && procIdx < processed.size()) {
            set.add(processed.get(procIdx).origIndex);
        }
    }

    // ================================================================
    // Step 7: Build student parts (filter + shuffle)
    // ================================================================

    private static Map<Integer, PartInfo> buildStudentParts(
            List<PartInfo> parts,
            List<ParaInfo> processed,
            Model model,
            Map<Integer, PartInfo> partByOrigStart) {

        Map<Integer, PartInfo> result = new LinkedHashMap<>();

        for (Map.Entry<Integer, PartInfo> entry : partByOrigStart.entrySet()) {
            PartInfo part = entry.getValue();

            List<QuestionInfo> filtered = new ArrayList<>();
            for (QuestionInfo q : part.questions) {
                if (shouldInclude(q.type, model)) filtered.add(q);
            }

            if (model.isShufflingOn()) {
                List<QuestionInfo> mcq  = new ArrayList<>();
                List<QuestionInfo> tf   = new ArrayList<>();
                List<QuestionInfo> blnk = new ArrayList<>();
                List<QuestionInfo> des  = new ArrayList<>();
                for (QuestionInfo q : filtered) {
                    switch (q.type) {
                        case "MCQ"  -> mcq.add(q);
                        case "TF"   -> tf.add(q);
                        case "BLANK"-> blnk.add(q);
                        default     -> des.add(q);
                    }
                }
                if (model.isMcqShuffled())        Collections.shuffle(mcq);
                if (model.isTrueFalseShuffled())  Collections.shuffle(tf);
                if (model.isBlankSpaceShuffled()) Collections.shuffle(blnk);
                if (model.isDesShuffled())        Collections.shuffle(des);

                filtered.clear();
                filtered.addAll(mcq);
                filtered.addAll(tf);
                filtered.addAll(blnk);
                filtered.addAll(des);
            }

            if (!filtered.isEmpty()) {
                PartInfo fp = new PartInfo();
                fp.title = part.title;
                fp.questions = filtered;
                result.put(entry.getKey(), fp);
            }
        }

        return result;
    }

    // ================================================================
    // Step 8: Write a single question to output doc
    // ================================================================

    private static void writeQuestion(QuestionInfo q, int qNum,
                                      List<XWPFParagraph> allOrigParas,
                                      List<ParaInfo> processed,
                                      XWPFDocument srcDoc,
                                      XWPFDocument outputDoc,
                                      Model model) {
        // Write stem
        if (q.stemParaIdx >= 0 && q.stemParaIdx < processed.size()) {
            int origIdx = processed.get(q.stemParaIdx).origIndex;
            if (origIdx < allOrigParas.size()) {
                copyParagraphWithImages(allOrigParas.get(origIdx), outputDoc, qNum);
            }
        } else {
            writeText(outputDoc, qNum + ". " + q.stemText);
        }

        if ("TF".equals(q.type)) {
            XWPFParagraph tfPara = outputDoc.createParagraph();
            XWPFRun tfRun = tfPara.createRun();
            tfRun.setText("    True          False");
            tfRun.setFontFamily("Arial");
            tfRun.setFontSize(12);
            return;
        }

        // Options — shuffle if enabled
        List<Integer> optIdxs  = new ArrayList<>(q.optionParaIdxs);
        List<String>  optTexts = new ArrayList<>(q.optionTexts);

        if (model.isOptionShufflingOn() && !optIdxs.isEmpty()) {
            List<Integer> order = new ArrayList<>();
            for (int oi = 0; oi < optIdxs.size(); oi++) order.add(oi);
            Collections.shuffle(order);
            List<Integer> si = new ArrayList<>();
            List<String> st = new ArrayList<>();
            for (int oi : order) { si.add(optIdxs.get(oi)); st.add(optTexts.get(oi)); }
            optIdxs = si; optTexts = st;
        }

        char letter = 'A';
        for (int oi = 0; oi < optIdxs.size(); oi++) {
            int procIdx = optIdxs.get(oi);
            if (procIdx >= 0 && procIdx < processed.size()) {
                int origIdx = processed.get(procIdx).origIndex;
                if (origIdx < allOrigParas.size()) {
                    copyOptionParagraphWithImages(allOrigParas.get(origIdx), outputDoc, letter++);
                    continue;
                }
            }
            // Fallback: write from text
            String optText = oi < optTexts.size() ? optTexts.get(oi) : "";
            writeText(outputDoc, "    " + letter++ + ") " + optText);
        }
    }

    // ================================================================
    // Get last original paragraph index of a part
    // ================================================================

    private static int getPartLastOrigIdx(PartInfo part, List<ParaInfo> processed) {
        int last = 0;
        for (QuestionInfo q : part.questions) {
            if (q.stemParaIdx >= 0 && q.stemParaIdx < processed.size())
                last = Math.max(last, processed.get(q.stemParaIdx).origIndex);
            for (int idx : q.optionParaIdxs) {
                if (idx >= 0 && idx < processed.size())
                    last = Math.max(last, processed.get(idx).origIndex);
            }
        }
        return last;
    }

    // ================================================================
    // Copy paragraph preserving text formatting + images
    // ================================================================

    private static void copyParagraphWithImages(XWPFParagraph src,
                                                XWPFDocument dest,
                                                int questionNumber) {
        XWPFParagraph newPara = dest.createParagraph();
        if (src.getCTP().getPPr() != null)
            newPara.getCTP().setPPr((CTPPr) src.getCTP().getPPr().copy());

        boolean firstRun = true;
        for (XWPFRun srcRun : src.getRuns()) {
            List<XWPFPicture> pics = srcRun.getEmbeddedPictures();
            XWPFRun newRun = newPara.createRun();
            if (srcRun.getCTR().getRPr() != null)
                newRun.getCTR().setRPr((CTRPr) srcRun.getCTR().getRPr().copy());

            if (!pics.isEmpty()) {
                for (XWPFPicture pic : pics) copyImage(pic, srcRun, newRun);
            } else {
                String text = srcRun.getText(0);
                if (text == null) text = "";
                if (firstRun && questionNumber >= 1) {
                    text = text.replaceFirst("^\\s*\\d+[.)\\-]\\s*", "").trim();
                    text = questionNumber + ". " + text;
                    firstRun = false;
                }
                newRun.setText(text);
            }
        }
        if (src.getRuns().isEmpty() && questionNumber >= 1)
            newPara.createRun().setText(questionNumber + ". ");
    }

    private static void copyOptionParagraphWithImages(XWPFParagraph src,
                                                      XWPFDocument dest,
                                                      char newLetter) {
        XWPFParagraph newPara = dest.createParagraph();
        if (src.getCTP().getPPr() != null)
            newPara.getCTP().setPPr((CTPPr) src.getCTP().getPPr().copy());

        boolean firstRun = true;
        for (XWPFRun srcRun : src.getRuns()) {
            List<XWPFPicture> pics = srcRun.getEmbeddedPictures();
            XWPFRun newRun = newPara.createRun();
            if (srcRun.getCTR().getRPr() != null)
                newRun.getCTR().setRPr((CTRPr) srcRun.getCTR().getRPr().copy());

            if (!pics.isEmpty()) {
                // Write new letter before the image
                if (firstRun) { newRun.setText("    " + newLetter + ") "); firstRun = false; }
                for (XWPFPicture pic : pics) copyImage(pic, srcRun, newRun);
            } else {
                String text = srcRun.getText(0);
                if (text == null) text = "";
                if (firstRun) {
                    String leading = text.replaceAll("^(\\s*).*", "$1");
                    String rest = text.replaceFirst("(?i)^\\s*[A-Da-d][.)\\-]\\s*", "")
                            .replaceFirst("^\\s*[1-4][.)\\-]\\s*", "");
                    text = leading + newLetter + ") " + rest;
                    firstRun = false;
                }
                newRun.setText(text);
            }
        }
    }

    private static void copyImage(XWPFPicture pic, XWPFRun srcRun, XWPFRun destRun) {
        try {
            byte[] data  = pic.getPictureData().getData();
            int picType  = pic.getPictureData().getPictureType();
            int w = 4500000, h = 3600000; // default ~5cm × 4cm in EMUs
            try {
                String xml = srcRun.getCTR().xmlText();
                Matcher mw = Pattern.compile("cx=\"(\\d+)\"").matcher(xml);
                Matcher mh = Pattern.compile("cy=\"(\\d+)\"").matcher(xml);
                if (mw.find()) w = Integer.parseInt(mw.group(1));
                if (mh.find()) h = Integer.parseInt(mh.group(1));
            } catch (Exception ignored) {}
            destRun.addPicture(new ByteArrayInputStream(data), picType,
                    pic.getPictureData().getFileName(), w, h);
        } catch (Exception e) {
            destRun.setText("[IMAGE]");
        }
    }

    private static void writeText(XWPFDocument dest, String text) {
        XWPFRun run = dest.createParagraph().createRun();
        run.setText(text); run.setFontFamily("Arial"); run.setFontSize(12);
    }

    // ================================================================
    // Perplexity API
    // ================================================================

    private static final int MAX_RETRIES = 3;

    private static List<PartInfo> analyzeWithPerplexity(String text, String apiKey) {

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            System.out.println("ExamGenerator Perplexity attempt " + attempt + "/" + MAX_RETRIES);
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("model", MODEL);
                body.put("max_tokens", 6000);
                body.put("temperature", 0);
                body.put("messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user",   "content", text)
                ));
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .timeout(Duration.ofSeconds(90))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                        .build();

                HttpResponse<String> resp =
                        HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() == 429) {
                    System.err.println("Rate limited — waiting 5s...");
                    Thread.sleep(5000);
                    continue;
                }
                if (resp.statusCode() != 200) {
                    System.err.println("Perplexity error " + resp.statusCode());
                    Thread.sleep(2000L * attempt);
                    continue;
                }

                String content = MAPPER.readTree(resp.body())
                        .path("choices").get(0).path("message").path("content").asText().trim();

                System.out.println("Perplexity response: " +
                        content.substring(0, Math.min(300, content.length())));

                // Validate completeness — VPN can truncate responses
                String clean = content
                        .replaceAll("(?s)```json\\s*", "")
                        .replaceAll("(?s)```\\s*", "").trim();

                if (!clean.startsWith("[") || !clean.endsWith("]")) {
                    System.err.println("Truncated JSON on attempt " + attempt + " — retrying...");
                    Thread.sleep(2000L * attempt);
                    continue;
                }

                List<PartInfo> parts = parseJson(content);
                if (parts == null || parts.isEmpty()) {
                    System.err.println("Empty result on attempt " + attempt + " — retrying...");
                    Thread.sleep(2000L * attempt);
                    continue;
                }

                System.out.println("Success on attempt " + attempt + " — "
                        + parts.size() + " parts found.");
                return parts;

            } catch (java.net.http.HttpTimeoutException e) {
                System.err.println("Timeout on attempt " + attempt + " (VPN slow) — retrying...");
                try { Thread.sleep(3000L * attempt); } catch (InterruptedException ignored) {}
            } catch (Exception e) {
                System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
                try { Thread.sleep(2000L * attempt); } catch (InterruptedException ignored) {}
            }
        }

        System.err.println("All " + MAX_RETRIES + " attempts failed.");
        return null;
    }

    private static List<PartInfo> parseJson(String json) {
        List<PartInfo> parts = new ArrayList<>();
        try {
            json = json.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();
            JsonNode arr = MAPPER.readTree(json);
            if (!arr.isArray()) return null;
            for (JsonNode pn : arr) {
                PartInfo part = new PartInfo();
                part.title = pn.path("partTitle").asText("");
                for (JsonNode qn : pn.path("questions")) {
                    QuestionInfo q = new QuestionInfo();
                    q.stemText = qn.path("stem").asText("").trim();
                    q.type     = qn.path("type").asText("DESCRIPTIVE").trim().toUpperCase();
                    for (JsonNode opt : qn.path("options")) {
                        String t = opt.asText().trim();
                        if (!t.isBlank()) q.optionTexts.add(t);
                    }
                    if (!q.stemText.isBlank()) part.questions.add(q);
                }
                if (!part.questions.isEmpty()) parts.add(part);
            }
        } catch (Exception e) { System.err.println("JSON parse failed: " + e.getMessage()); return null; }
        return parts;
    }

    private static boolean shouldInclude(String type, Model model) {
        return switch (type) {
            case "MCQ"  -> model.isMCQ();
            case "TF"   -> model.isTrueFalse();
            case "BLANK"-> model.isBlank();
            default     -> model.isDes();
        };
    }

    // ================================================================
    // Data classes
    // ================================================================

    private static class ParaInfo {
        int     origIndex;
        String  rawText;
        boolean hasImage;
        String  displayText;

        ParaInfo(int origIndex, String rawText, boolean hasImage) {
            this.origIndex   = origIndex;
            this.rawText     = rawText;
            this.hasImage    = hasImage;
            this.displayText = rawText;
        }
    }

    private static class PartInfo {
        String title = "";
        List<QuestionInfo> questions = new ArrayList<>();
    }

    private static class QuestionInfo {
        String        type          = "DESCRIPTIVE";
        String        stemText      = "";
        List<String>  optionTexts   = new ArrayList<>();
        int           stemParaIdx   = -1;
        List<Integer> optionParaIdxs = new ArrayList<>();
    }
}
