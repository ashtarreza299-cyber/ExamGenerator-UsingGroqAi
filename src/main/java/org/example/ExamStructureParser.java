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
 * Reads a .docx exam file and uses Groq AI to parse it into an ExamStructure:
 *   - header (university info, student name fields, etc.)
 *   - parts (each with a title and list of questions)
 *
 * Groq returns structured JSON which is then mapped to ExamStructure objects.
 */
public class ExamStructureParser {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.1-8b-instant";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    // ----------------------------------------------------------------
    // System prompt — tells Groq to return structured JSON
    // ----------------------------------------------------------------

    private static final String SYSTEM_PROMPT =
        "You are an exam document parser. Given raw exam text, extract and return ONLY a JSON object " +
        "with this exact structure (no markdown, no explanation, just JSON):\n" +
        "{\n" +
        "  \"header\": [\"line1\", \"line2\", ...],\n" +
        "  \"parts\": [\n" +
        "    {\n" +
        "      \"title\": \"Part One: Multiple Choice Questions\",\n" +
        "      \"questions\": [\n" +
        "        {\n" +
        "          \"stem\": \"What is Java?\",\n" +
        "          \"type\": \"MCQ\",\n" +
        "          \"options\": [\"Object Oriented Language\", \"Database\", \"Browser\", \"OS\"]\n" +
        "        },\n" +
        "        {\n" +
        "          \"stem\": \"Explain polymorphism.\",\n" +
        "          \"type\": \"DESCRIPTIVE\",\n" +
        "          \"options\": []\n" +
        "        }\n" +
        "      ]\n" +
        "    }\n" +
        "  ]\n" +
        "}\n\n" +
        "Rules:\n" +
        "- header: all lines before the first question (university name, course, date, student name fields, etc.)\n" +
        "- parts: group questions by their section. If no explicit section titles exist, group by question type.\n" +
        "- type must be one of: MCQ, TF, BLANK, DESCRIPTIVE\n" +
        "- For MCQ, options must NOT include the A) B) C) D) prefix — just the text\n" +
        "- Return ONLY the JSON. No explanation. No markdown. No backticks.";

    // ----------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------

    /**
     * Parse the given .docx file into an ExamStructure using Groq AI.
     * Returns null if parsing fails.
     */
    public static ExamStructure parse(File file, String apiKey) {

        // 1. Read all text from the Word file
        String rawText = readDocx(file);
        if (rawText == null || rawText.isBlank()) {
            System.err.println("ExamStructureParser: file is empty or unreadable.");
            return null;
        }

        // 2. Send to Groq
        String json = callGroq(rawText, apiKey);
        if (json == null) {
            System.err.println("ExamStructureParser: Groq returned null.");
            return null;
        }

        // 3. Parse JSON into ExamStructure
        return parseJson(json);
    }

    // ----------------------------------------------------------------
    // Read .docx into plain text
    // ----------------------------------------------------------------

    private static String readDocx(File file) {

        StringBuilder sb = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {

            for (XWPFParagraph para : doc.getParagraphs()) {
                String text = para.getText();
                if (text != null) sb.append(text).append("\n");
            }

        } catch (Exception e) {
            System.err.println("ExamStructureParser: could not read docx — " + e.getMessage());
            return null;
        }

        return sb.toString();
    }

    // ----------------------------------------------------------------
    // Call Groq API
    // ----------------------------------------------------------------

    private static String callGroq(String examText, String apiKey) {

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", MODEL);
            body.put("max_tokens", 4000);
            body.put("temperature", 0);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user",   "content", examText)
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
                System.err.println("Groq error " + response.statusCode()
                        + ": " + response.body());
                return null;
            }

            JsonNode root = MAPPER.readTree(response.body());
            return root.path("choices")
                       .get(0)
                       .path("message")
                       .path("content")
                       .asText()
                       .trim();

        } catch (Exception e) {
            System.err.println("ExamStructureParser Groq call failed: " + e.getMessage());
            return null;
        }
    }

    // ----------------------------------------------------------------
    // Parse JSON response into ExamStructure
    // ----------------------------------------------------------------

    private static ExamStructure parseJson(String json) {

        try {
            // Strip markdown code fences if Groq adds them despite instructions
            json = json.replaceAll("(?s)```json\\s*", "")
                       .replaceAll("(?s)```\\s*", "")
                       .trim();

            JsonNode root = MAPPER.readTree(json);
            ExamStructure structure = new ExamStructure();

            // Parse header lines
            JsonNode headerNode = root.path("header");
            if (headerNode.isArray()) {
                for (JsonNode line : headerNode) {
                    String text = line.asText().trim();
                    if (!text.isBlank()) structure.addHeaderLine(text);
                }
            }

            // Parse parts
            JsonNode partsNode = root.path("parts");
            if (partsNode.isArray()) {
                for (JsonNode partNode : partsNode) {

                    String partTitle = partNode.path("title").asText("").trim();
                    ExamStructure.ExamPart part = new ExamStructure.ExamPart(partTitle);

                    JsonNode questionsNode = partNode.path("questions");
                    if (questionsNode.isArray()) {
                        for (JsonNode qNode : questionsNode) {

                            String stem = qNode.path("stem").asText("").trim();
                            String type = qNode.path("type").asText("DESCRIPTIVE").trim().toUpperCase();

                            List<String> options = new ArrayList<>();
                            JsonNode optionsNode = qNode.path("options");
                            if (optionsNode.isArray()) {
                                for (JsonNode opt : optionsNode) {
                                    String optText = opt.asText().trim();
                                    if (!optText.isBlank()) options.add(optText);
                                }
                            }

                            part.addQuestion(new ExamStructure.ExamQuestion(stem, options, type));
                        }
                    }

                    structure.addPart(part);
                }
            }

            System.out.println("ExamStructureParser: parsed "
                    + structure.getParts().size() + " parts, "
                    + structure.totalQuestions() + " total questions.");

            return structure;

        } catch (Exception e) {
            System.err.println("ExamStructureParser: JSON parse failed — " + e.getMessage());
            return null;
        }
    }
}
