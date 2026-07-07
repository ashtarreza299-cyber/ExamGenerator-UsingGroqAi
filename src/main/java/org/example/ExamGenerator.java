package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/**
 * Generates shuffled exams for all students in one Word file.
 *
 * Strategy:
 *  1. Send the exam text to Groq — get back the index ranges of each
 *     question group (which paragraph indices belong to which part).
 *  2. For each student, copy the original document, then reorder
 *     only the question paragraphs within each part.
 *  3. Append all student copies into one output file with page breaks.
 *
 * All original formatting (fonts, bold, RTL Persian, tables, borders)
 * is preserved because we never rebuild — we only reorder.
 */
public class ExamGenerator {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.3-70b-versatile"; // better for Persian
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    // ----------------------------------------------------------------
    // System prompt — asks Groq to identify paragraph index ranges
    // of each question within each part
    // ----------------------------------------------------------------

    private static final String SYSTEM_PROMPT =
        "You are an exam document analyzer. You will receive a list of paragraphs from an exam " +
        "Word file, each prefixed with its index like: '0: Kabul University', '1: Name: ___', etc.\n\n" +
        "Your job is to identify the exam PARTS and within each part, identify which paragraph " +
        "index ranges belong to each individual QUESTION (including its options/lines).\n\n" +
        "Return ONLY a JSON array. No markdown. No explanation. Example:\n" +
        "[\n" +
        "  {\n" +
        "    \"partTitle\": \"Part One: MCQ\",\n" +
        "    \"questions\": [\n" +
        "      {\"start\": 5, \"end\": 9},\n" +
        "      {\"start\": 10, \"end\": 14}\n" +
        "    ]\n" +
        "  },\n" +
        "  {\n" +
        "    \"partTitle\": \"Part Two: Descriptive\",\n" +
        "    \"questions\": [\n" +
        "      {\"start\": 16, \"end\": 16},\n" +
        "      {\"start\": 17, \"end\": 17}\n" +
        "    ]\n" +
        "  }\n" +
        "]\n\n" +
        "Rules:\n" +
        "- start and end are inclusive paragraph indices\n" +
        "- A question's range includes its stem AND all its option lines\n" +
        "- Do NOT include header lines, part titles, instructions, name fields in any question range\n" +
        "- The document may be in Persian, English, or mixed — handle both\n" +
        "- Return ONLY the JSON array. Nothing else.";

    // ----------------------------------------------------------------
    // Public entry point — called from DashBoardPanel.generateExam()
    // ----------------------------------------------------------------

    public static void generate(JPanel parent, Model model) {

        // Safety checks
        if (model.getOutputFolder() == null) {
            JOptionPane.showMessageDialog(parent, "Please select output folder!");
            return;
        }
        if (model.getSelectedFile() == null) {
            JOptionPane.showMessageDialog(parent, "Please load a Word file first!");
            return;
        }
        int studentCount = model.getNumberOfStudents();
        if (studentCount <= 0) {
            JOptionPane.showMessageDialog(parent, "Please set number of students!");
            return;
        }

        String apiKey = ApiKeyManager.loadKey();
        if (apiKey == null) {
            JOptionPane.showMessageDialog(parent,
                    "Groq API key not found!\nPlease restart the app to set up your key.");
            return;
        }

        // Run in background so UI stays responsive
        JOptionPane.showMessageDialog(parent,
                "Analyzing exam structure...\nThis may take a few seconds.");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {

                // 1. Read all paragraphs from original file
                List<String> paragraphTexts = new ArrayList<>();
                try (FileInputStream fis = new FileInputStream(model.getSelectedFile());
                     XWPFDocument doc = new XWPFDocument(fis)) {
                    for (XWPFParagraph p : doc.getParagraphs()) {
                        paragraphTexts.add(p.getText() == null ? "" : p.getText());
                    }
                }

                // 2. Build indexed paragraph list for Groq
                StringBuilder indexed = new StringBuilder();
                for (int i = 0; i < paragraphTexts.size(); i++) {
                    indexed.append(i).append(": ").append(paragraphTexts.get(i)).append("\n");
                }

                // 3. Ask Groq to identify question index ranges per part
                List<PartInfo> parts = analyzeWithGroq(indexed.toString(), apiKey);
                if (parts == null || parts.isEmpty()) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(parent,
                                    "Could not analyze exam structure. Please check your file."));
                    return null;
                }

                // 4. Build output document
                try (XWPFDocument outputDoc = new XWPFDocument()) {

                    for (int s = 0; s < studentCount; s++) {

                        // Re-read original doc for each student to get fresh paragraphs
                        try (FileInputStream fis =
                                     new FileInputStream(model.getSelectedFile());
                             XWPFDocument srcDoc = new XWPFDocument(fis)) {

                            List<XWPFParagraph> allParas = srcDoc.getParagraphs();

                            // Track which paragraph indices are question paragraphs
                            // so we know what to reorder vs what to copy as-is
                            Set<Integer> questionIndices = new HashSet<>();
                            for (PartInfo part : parts) {
                                for (int[] range : part.questionRanges) {
                                    for (int i = range[0]; i <= range[1]; i++) {
                                        questionIndices.add(i);
                                    }
                                }
                            }

                            // Build shuffled question order per part
                            Map<Integer, List<int[]>> shuffledParts = new LinkedHashMap<>();
                            for (PartInfo part : parts) {
                                List<int[]> ranges = new ArrayList<>(part.questionRanges);
                                if (model.isShufflingOn()) Collections.shuffle(ranges);
                                // Map first paragraph index of part to shuffled ranges
                                shuffledParts.put(part.questionRanges.get(0)[0], ranges);
                            }

                            // Write student label
                            XWPFParagraph labelPara = outputDoc.createParagraph();
                            labelPara.setAlignment(ParagraphAlignment.CENTER);
                            XWPFRun labelRun = labelPara.createRun();
                            labelRun.setText("Student " + (s + 1));
                            labelRun.setBold(true);
                            labelRun.setFontSize(14);
                            outputDoc.createParagraph(); // empty line after label

                            // Walk through all paragraphs in order
                            int i = 0;
                            while (i < allParas.size()) {

                                // Check if this index is the START of a question group
                                // in any part, and if we have a shuffled order for it
                                PartInfo currentPart = getPartStartingAt(parts, i);

                                if (currentPart != null) {
                                    // Write all questions in shuffled order
                                    List<int[]> shuffled =
                                            shuffledParts.get(currentPart.questionRanges.get(0)[0]);
                                    int qNum = 1;
                                    for (int[] range : shuffled) {
                                        // Write question number on first paragraph of question
                                        boolean firstLine = true;
                                        for (int qi = range[0]; qi <= range[1]; qi++) {
                                            if (qi < allParas.size()) {
                                                copyParagraph(allParas.get(qi),
                                                        outputDoc, firstLine ? qNum : -1);
                                                firstLine = false;
                                            }
                                        }
                                        qNum++;
                                        outputDoc.createParagraph(); // spacing between questions
                                    }
                                    // Skip past all paragraphs belonging to this part
                                    int lastIdx = currentPart.questionRanges
                                            .get(currentPart.questionRanges.size() - 1)[1];
                                    i = lastIdx + 1;

                                } else if (questionIndices.contains(i)) {
                                    // Already handled above — skip
                                    i++;
                                } else {
                                    // Non-question paragraph — copy as-is (header, title, etc.)
                                    copyParagraph(allParas.get(i), outputDoc, -1);
                                    i++;
                                }
                            }

                            // Page break between students
                            if (s < studentCount - 1) {
                                XWPFParagraph pb = outputDoc.createParagraph();
                                pb.setPageBreak(true);
                            }
                        }
                    }

                    // 5. Save output file
                    String fileName = (model.getFileNaming() != null
                            && !model.getFileNaming().isBlank())
                            ? model.getFileNaming() : "Exam";
                    String finalName = fileName + "_" + model.getStartNumber() + ".docx";
                    File outFile = new File(model.getOutputFolder(), finalName);

                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        outputDoc.write(fos);
                    }

                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(parent,
                                    "✓ Exam generated successfully!\n"
                                    + studentCount + " student exams saved to:\n"
                                    + outFile.getAbsolutePath()));
                }

                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // surface any exceptions
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(parent,
                                    "Error generating exam: " + ex.getMessage()));
                }
            }
        };

        worker.execute();
    }

    // ----------------------------------------------------------------
    // Ask Groq to identify question paragraph ranges per part
    // ----------------------------------------------------------------

    private static List<PartInfo> analyzeWithGroq(String indexedText, String apiKey) {

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", MODEL);
            body.put("max_tokens", 4000);
            body.put("temperature", 0);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user",   "content", indexedText)
            ));

            String requestBody = MAPPER.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Groq error " + response.statusCode());
                return null;
            }

            String content = MAPPER.readTree(response.body())
                    .path("choices").get(0)
                    .path("message").path("content")
                    .asText().trim();

            return parsePartInfo(content);

        } catch (Exception e) {
            System.err.println("ExamGenerator Groq call failed: " + e.getMessage());
            return null;
        }
    }

    // ----------------------------------------------------------------
    // Parse Groq JSON into PartInfo list
    // ----------------------------------------------------------------

    private static List<PartInfo> parsePartInfo(String json) {

        List<PartInfo> parts = new ArrayList<>();

        try {
            json = json.replaceAll("(?s)```json\\s*", "")
                       .replaceAll("(?s)```\\s*", "")
                       .trim();

            JsonNode array = MAPPER.readTree(json);
            if (!array.isArray()) return null;

            for (JsonNode partNode : array) {

                PartInfo part = new PartInfo();
                part.title = partNode.path("partTitle").asText("");

                JsonNode questionsNode = partNode.path("questions");
                if (questionsNode.isArray()) {
                    for (JsonNode q : questionsNode) {
                        int start = q.path("start").asInt();
                        int end   = q.path("end").asInt();
                        part.questionRanges.add(new int[]{start, end});
                    }
                }

                if (!part.questionRanges.isEmpty()) parts.add(part);
            }

        } catch (Exception e) {
            System.err.println("ExamGenerator JSON parse failed: " + e.getMessage());
            return null;
        }

        return parts;
    }

    // ----------------------------------------------------------------
    // Copy a paragraph from source doc to output doc
    // If questionNumber >= 1, prepend "N. " to the first run
    // ----------------------------------------------------------------

    private static void copyParagraph(XWPFParagraph src,
                                       XWPFDocument dest,
                                       int questionNumber) {
        XWPFParagraph newPara = dest.createParagraph();

        // Copy paragraph-level properties (alignment, RTL, spacing, etc.)
        if (src.getCTP().getPPr() != null) {
            newPara.getCTP().setPPr(
                    (org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr)
                    src.getCTP().getPPr().copy());
        }

        boolean firstRun = true;

        for (XWPFRun srcRun : src.getRuns()) {

            XWPFRun newRun = newPara.createRun();

            // Copy run properties (font, size, bold, italic, color, RTL, etc.)
            if (srcRun.getCTR().getRPr() != null) {
                newRun.getCTR().setRPr(
                        (org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr)
                        srcRun.getCTR().getRPr().copy());
            }

            String text = srcRun.getText(0);
            if (text == null) text = "";

            // Prepend question number to first run of question stem
            if (firstRun && questionNumber >= 1) {
                text = questionNumber + ". " + text;
                firstRun = false;
            }

            newRun.setText(text);
        }

        // If source had no runs but we need to write a question number
        if (src.getRuns().isEmpty() && questionNumber >= 1) {
            XWPFRun r = newPara.createRun();
            r.setText(questionNumber + ". ");
        }
    }

    // ----------------------------------------------------------------
    // Find if a part's questions start at paragraph index i
    // ----------------------------------------------------------------

    private static PartInfo getPartStartingAt(List<PartInfo> parts, int index) {
        for (PartInfo part : parts) {
            if (!part.questionRanges.isEmpty()
                    && part.questionRanges.get(0)[0] == index) {
                return part;
            }
        }
        return null;
    }

    // ----------------------------------------------------------------
    // Data class
    // ----------------------------------------------------------------

    private static class PartInfo {
        String title = "";
        List<int[]> questionRanges = new ArrayList<>();
    }
}
