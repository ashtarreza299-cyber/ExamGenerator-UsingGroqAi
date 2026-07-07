package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Sends the raw Word file text to Groq and gets back ONLY
 * the real exam questions with their types.
 * Falls back to tag-based parsing if offline or Groq fails.
 */
public class QuestionExtractor {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.3-70b-versatile";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private static final String SYSTEM_PROMPT =
            "You are an exam question extractor. Given raw exam text, extract ONLY the actual exam questions.\n\n"
                    + "STRICTLY IGNORE — do NOT include these as questions:\n"
                    + "  - University name, faculty name, department name\n"
                    + "  - Course name, subject name, exam title\n"
                    + "  - Date, semester, class, year\n"
                    + "  - Student name fields (Name:___, F/Name:___, Student ID:___)\n"
                    + "  - Lecturer or teacher name\n"
                    + "  - Part titles (Part One, Part Two, Section 1, etc.)\n"
                    + "  - Instructions (Answer all questions, Use blue pen, etc.)\n"
                    + "  - Marks breakdown (10 x 1 = 10 Marks, Total: 50, etc.)\n"
                    + "  - Good Luck, Best of Luck\n"
                    + "  - Page numbers, headers, footers\n"
                    + "  - Empty lines or decorative lines\n\n"
                    + "QUESTION TYPES — classify CAREFULLY:\n"
                    + "  MCQ         = Has lettered or numbered options A) B) C) D)\n"
                    + "  TF          = Question where answer is True OR False\n"
                    + "  BLANK       = Has blank spaces like ______ or ........ to fill in\n"
                    + "  DESCRIPTIVE = Open ended: explain, describe, compare, write, discuss\n\n"
                    + "CRITICAL: DO NOT classify university info, part titles, or instructions as questions.\n"
                    + "CRITICAL: DO NOT classify everything as DESCRIPTIVE — read each line carefully.\n"
                    + "Document may be Persian, English, Dari, Arabic, or mixed.\n\n"
                    + "Return ONLY a JSON array. No markdown. No backticks. No explanation.\n"
                    + "[\n"
                    + "  {\"type\": \"MCQ\", \"stem\": \"What is Java?\", \"options\": [\"A language\", \"A database\", \"A browser\", \"An OS\"]},\n"
                    + "  {\"type\": \"TF\", \"stem\": \"Java is platform independent.\", \"options\": []},\n"
                    + "  {\"type\": \"BLANK\", \"stem\": \"The keyword to create an object is ______.\", \"options\": []},\n"
                    + "  {\"type\": \"DESCRIPTIVE\", \"stem\": \"Explain polymorphism with an example.\", \"options\": []}\n"
                    + "]\n\n"
                    + "Rules:\n"
                    + "- type must be one of: MCQ, TF, BLANK, DESCRIPTIVE\n"
                    + "- For MCQ: options must NOT include A) B) C) D) prefix, just the option text\n"
                    + "- For TF, BLANK, DESCRIPTIVE: options must be an empty array []\n"
                    + "- Return ONLY the JSON array. Nothing else.";

    // ----------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------

    public static List<Question> extract(File file, Model model) {

        String rawText = readDocx(file);
        if (rawText == null || rawText.isBlank()) return new ArrayList<>();

        String apiKey = ApiKeyManager.loadKey();

        if (apiKey != null && NetworkUtils.isOnline()) {
            List<Question> questions = extractWithGroq(rawText, apiKey, model);
            if (questions != null && !questions.isEmpty()) {
                System.out.println("Groq extracted " + questions.size() + " questions.");
                return questions;
            }
            System.err.println("Groq extraction failed — falling back to tag parser.");
        }

        return QuestionParser.parse(file, model);
    }

    // ----------------------------------------------------------------
    // Groq extraction
    // ----------------------------------------------------------------

    private static List<Question> extractWithGroq(String rawText, String apiKey, Model model) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", MODEL);
            body.put("max_tokens", 4000);
            body.put("temperature", 0);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user",   "content", rawText)
            ));

            String requestBody = MAPPER.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(60))
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

            JsonNode root    = MAPPER.readTree(response.body());
            String   content = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

            System.out.println("Groq raw: " + content.substring(0, Math.min(300, content.length())));

            return parseJsonQuestions(content, model);

        } catch (Exception e) {
            System.err.println("QuestionExtractor Groq call failed: " + e.getMessage());
            return null;
        }
    }

    // ----------------------------------------------------------------
    // Parse JSON array into Question objects
    // ----------------------------------------------------------------

    private static List<Question> parseJsonQuestions(String json, Model model) {

        List<Question> questions = new ArrayList<>();

        try {
            json = json.replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            JsonNode array = MAPPER.readTree(json);
            if (!array.isArray()) {
                System.err.println("QuestionExtractor: expected JSON array.");
                return null;
            }

            for (JsonNode node : array) {

                String type = node.path("type").asText("DESCRIPTIVE").trim().toUpperCase();
                String stem = node.path("stem").asText("").trim();

                if (stem.isBlank()) continue;

                List<String> options = new ArrayList<>();
                JsonNode optionsNode = node.path("options");
                if (optionsNode.isArray()) {
                    for (JsonNode opt : optionsNode) {
                        String optText = opt.asText().trim();
                        if (!optText.isBlank()) options.add(optText);
                    }
                }

                switch (type) {
                    case "MCQ"         -> questions.add(new MultipleChoiceQuestion(stem, options, model));
                    case "TF"          -> questions.add(new TrueFalseQuestion(stem));
                    case "BLANK"       -> questions.add(new BlankSpaceQuestion(stem));
                    case "DESCRIPTIVE" -> questions.add(new DescriptiveQuestion(stem));
                    default            -> questions.add(new DescriptiveQuestion(stem));
                }
            }

        } catch (Exception e) {
            System.err.println("QuestionExtractor JSON parse failed: " + e.getMessage());
            return null;
        }

        return questions;
    }

    // ----------------------------------------------------------------
    // Read .docx into plain text
    // Handles tab-separated options and marks image paragraphs
    // ----------------------------------------------------------------

    private static String readDocx(File file) {

        StringBuilder sb = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {

            for (XWPFParagraph para : doc.getParagraphs()) {

                String text = para.getText();
                if (text == null) text = "";

                String xml    = para.getCTP().xmlText();
                boolean hasImg = xml.contains("w:drawing") || xml.contains("w:pict");

                // Expand tab-separated options on same line
                // e.g. "    A) this\t\tB) super" → two separate lines
                if (text.contains("\t")
                        && text.trim().matches("(?i).*[A-Da-d][.)\\]].*[A-Da-d][.)\\]].*")) {
                    String[] parts = text.split("\t+");
                    for (String part : parts) {
                        String p = part.trim();
                        if (!p.isEmpty()) sb.append(p).append("\n");
                    }
                    continue;
                }

                if (!text.isBlank() || hasImg) {
                    if (hasImg && text.isBlank())  sb.append("[IMAGE]\n");
                    else if (hasImg)               sb.append(text).append(" [IMAGE]\n");
                    else                           sb.append(text).append("\n");
                }
            }

        } catch (Exception e) {
            System.err.println("QuestionExtractor: could not read file — " + e.getMessage());
            return null;
        }

        return sb.toString();
    }
}
