package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends question blocks to Groq AI for classification.
 * Groq is free and requires no payment — just a free account at console.groq.com
 */
public class GroqClassifier {

    private static final String API_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private static final String MODEL = "llama3-8b-8192"; // Free Groq model

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String SYSTEM_PROMPT =
            "You are an exam question classifier. " +
            "Given a question block, reply with ONLY one of these four labels:\n" +
            "  MCQ         — multiple-choice question (has lettered/numbered options)\n" +
            "  TF          — true/false question\n" +
            "  BLANK       — fill-in-the-blank question\n" +
            "  DESCRIPTIVE — open-ended / essay question\n" +
            "Reply with the label only. No explanation. No punctuation.";

    // ----------------------------------------------------------------
    // Verify a key is valid by sending a tiny test request
    // ----------------------------------------------------------------

    public static boolean verifyKey(String apiKey) {
        try {
            String label = callGroq("What is 2+2?", apiKey);
            return label != null; // Any valid response means the key works
        } catch (Exception e) {
            return false;
        }
    }

    // ----------------------------------------------------------------
    // Classify a single question block
    // ----------------------------------------------------------------

    public static String classify(String text, String apiKey) {
        try {
            return callGroq(text, apiKey);
        } catch (Exception e) {
            System.err.println("Groq error: " + e.getMessage());
            return null; // Caller will fall back to tags
        }
    }

    // ----------------------------------------------------------------
    // Core HTTP call
    // ----------------------------------------------------------------

    private static String callGroq(String text, String apiKey) throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("max_tokens", 10);
        body.put("temperature", 0);
        body.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user",   "content", text)
        ));

        String requestBody = MAPPER.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response =
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Groq HTTP " + response.statusCode());
            return null;
        }

        JsonNode root = MAPPER.readTree(response.body());
        return root.path("choices")
                   .get(0)
                   .path("message")
                   .path("content")
                   .asText()
                   .trim()
                   .toUpperCase();
    }
}
