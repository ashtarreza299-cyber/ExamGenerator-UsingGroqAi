package org.example.service;

import org.example.model.BlankSpaceQuestion;
import org.example.model.DescriptiveQuestion;
import org.example.model.ExamStructure;
import org.example.model.Model;
import org.example.model.ModelListener;
import org.example.model.MultipleChoiceQuestion;
import org.example.model.Question;
import org.example.model.TrueFalseQuestion;

import java.util.ArrayList;
import java.util.List;

public class QuestionClassifier {

    private static String groqApiKey = null;

    public static void setApiKey(String key) {
        groqApiKey = key;
    }

    public static Question classify(String rawText, Model model) {

        if (rawText == null || rawText.isBlank()) return null;
        String text = rawText.trim();

        if (groqApiKey != null) {
            String label = GroqClassifier.classify(text, groqApiKey);
            if (label != null) return buildQuestion(label, text, model);
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

    private static Question buildQuestion(String label, String text, Model model) {
        return switch (label) {
            case "MCQ"         -> buildMCQ(text, model);
            case "TF"          -> new TrueFalseQuestion(text);
            case "BLANK"       -> new BlankSpaceQuestion(text);
            case "DESCRIPTIVE" -> new DescriptiveQuestion(text);
            default            -> new DescriptiveQuestion(text);
        };
    }

    private static Question classifyWithTags(String text, Model model) {

        String upper = text.toUpperCase();

        if (upper.startsWith("[M]")) return buildMCQ(text.substring(3).trim(), model);
        if (upper.startsWith("[T]")) return new TrueFalseQuestion(text.substring(3).trim());
        if (upper.startsWith("[B]")) return new BlankSpaceQuestion(text.substring(3).trim());
        if (upper.startsWith("[D]")) return new DescriptiveQuestion(text.substring(3).trim());

        return new DescriptiveQuestion(text);
    }

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
