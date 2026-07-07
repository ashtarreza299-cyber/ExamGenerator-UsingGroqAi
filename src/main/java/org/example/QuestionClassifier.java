package org.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Classifies question blocks using:
 *   - Groq AI (free) when a key is available
 *   - Legacy [M] / [D] / [T] / [B] tags as fallback
 */
public class QuestionClassifier {

    // Loaded once at startup by ApiKeyManager
    private static String groqApiKey = null;

    public static void setApiKey(String key) {
        groqApiKey = key;
    }

    // ----------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------

    public static Question classify(String rawText, Model model) {

        if (rawText == null || rawText.isBlank()) return null;
        String text = rawText.trim();

        if (groqApiKey != null) {
            String label = GroqClassifier.classify(text, groqApiKey);
            if (label != null) return buildQuestion(label, text, model);
            // Groq failed — fall through to tags
            System.err.println("Groq failed — falling back to tag detection.");
        }

        return classifyWithTags(text, model);
    }

    public static List<Question> classifyAll(List<String> rawTexts, Model model) {

        List<Question> result = new ArrayList<>();
        for (String raw : rawTexts) {
            Question q = classify(raw, model);
            if (q != null) result.add(q);
        }
        return result;
    }

    // ----------------------------------------------------------------
    // Build Question from Groq label
    // ----------------------------------------------------------------

    private static Question buildQuestion(String label, String text, Model model) {
        return switch (label) {
            case "MCQ"         -> buildMCQ(text, model);
            case "TF"          -> new TrueFalseQuestion(text);
            case "BLANK"       -> new BlankSpaceQuestion(text);
            case "DESCRIPTIVE" -> new DesceiptiveQuestion(text);
            default            -> new DesceiptiveQuestion(text);
        };
    }

    // ----------------------------------------------------------------
    // Offline fallback — [M] / [T] / [B] / [D] tags
    // ----------------------------------------------------------------

    private static Question classifyWithTags(String text, Model model) {

        String upper = text.toUpperCase();

        if (upper.startsWith("[M]")) return buildMCQ(text.substring(3).trim(), model);
        if (upper.startsWith("[T]")) return new TrueFalseQuestion(text.substring(3).trim());
        if (upper.startsWith("[B]")) return new BlankSpaceQuestion(text.substring(3).trim());
        if (upper.startsWith("[D]")) return new DesceiptiveQuestion(text.substring(3).trim());

        System.err.println("No tag found for: \""
                + text.substring(0, Math.min(50, text.length())) + "…\"");
        return new DesceiptiveQuestion(text);
    }

    // ----------------------------------------------------------------
    // MCQ helper — splits stem from option lines
    // ----------------------------------------------------------------

    private static Question buildMCQ(String text, Model model) {

        List<String> options = new ArrayList<>();
        StringBuilder stem   = new StringBuilder();

        for (String line : text.split("\\r?\\n")) {
            String t = line.trim();
            if (t.matches("(?i)^([A-Ea-e]|[1-5])[.)\\]]\\s+.+")) {
                options.add(t.replaceFirst("^([A-Ea-e]|[1-5])[.)\\]]\\s+", "").trim());
            } else {
                if (!stem.isEmpty()) stem.append("\n");
                stem.append(t);
            }
        }

        return new MultipleChoiceQuestion(stem.toString().trim(), options, model);
    }
}
