package org.example;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class QuestionParser {

    private static final Set<String> KNOWN_TAGS = Set.of(
            "[M]", "[D]", "[B]", "[T]", "[T/F]"
    );

    public static List<Question> parse(File file, Model model) {

        List<Question> questions = new ArrayList<>();
        List<String> lines = readLines(file);
        if (lines.isEmpty()) return questions;

        if (NetworkUtils.isOnline()) {
            parseOnline(lines, questions, model);
        } else {
            parseOffline(lines, questions, model);
        }

        return questions;
    }

    private static void parseOnline(List<String> lines, List<Question> out, Model model) {
        List<String> block = new ArrayList<>();

        for (String line : lines) {
            if (line.isBlank()) {
                flushOnlineBlock(block, out, model);
                block.clear();
            } else {
                block.add(line);
            }
        }
        flushOnlineBlock(block, out, model);
    }

    private static void flushOnlineBlock(List<String> lines, List<Question> out, Model model) {
        if (lines.isEmpty()) return;
        String raw = String.join("\n", lines).trim();
        if (raw.isBlank()) return;
        Question q = QuestionClassifier.classify(raw, model);
        if (q != null) out.add(q);
    }

    private static void parseOffline(List<String> lines, List<Question> out, Model model) {

        String currentTag  = null;
        List<String> block = new ArrayList<>();

        for (String line : lines) {

            String trimmed = line.trim();

            // Always skip blank lines offline
            if (trimmed.isBlank()) continue;

            // Check if line STARTS with a known tag
            String foundTag    = extractTag(trimmed);
            String afterTag    = extractAfterTag(trimmed);

            if (foundTag != null) {

                // Flush previous block first
                if (currentTag != null) {
                    flushTaggedBlock(currentTag, block, out, model);
                    block.clear();
                }

                currentTag = foundTag;

                // If there is text after the tag on the same line, add it to the block
                // e.g. "[M] What is Java?" → afterTag = "What is Java?"
                if (!afterTag.isBlank()) {
                    block.add(afterTag);
                }

            } else {
                // Regular content line — add to current block
                block.add(trimmed);
            }
        }

        // Flush the last block
        if (currentTag != null) {
            flushTaggedBlock(currentTag, block, out, model);
        }
    }

    private static void flushTaggedBlock(String tag, List<String> lines, List<Question> out, Model model) {
        if (lines.isEmpty()) return;

        switch (tag) {

            case "[M]" -> {
                String stem = lines.get(0);
                List<String> options = new ArrayList<>();
                for (int i = 1; i < lines.size(); i++) {
                    String opt = lines.get(i).trim();
                    if (!opt.isBlank()) {
                        // Strip A) B) 1. etc prefix if present
                        opt = opt.replaceFirst(
                                "(?i)^([A-Ea-e]|[1-5])[.)\\]]\\s+", "").trim();
                        options.add(opt);
                    }
                }
                out.add(new MultipleChoiceQuestion(stem, options, model));
            }

            case "[T]", "[T/F]" -> {
                // First content line = question text
                // "True" / "False" lines that follow are ignored
                out.add(new TrueFalseQuestion(lines.get(0)));
            }

            case "[B]" -> {
                out.add(new BlankSpaceQuestion(lines.get(0)));
            }

            case "[D]" -> {
                out.add(new DescriptiveQuestion(lines.get(0)));
            }

            default -> System.err.println("Unknown tag: " + tag);
        }
    }

    private static String extractTag(String line) {
        for (String tag : KNOWN_TAGS) {
            if (line.toUpperCase().startsWith(tag)) {
                return tag;
            }
        }
        return null;
    }

    private static String extractAfterTag(String line) {
        for (String tag : KNOWN_TAGS) {
            if (line.toUpperCase().startsWith(tag)) {
                return line.substring(tag.length()).trim();
            }
        }
        return "";
    }

    private static List<String> readLines(File file) {

        List<String> lines = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {

            for (XWPFParagraph para : doc.getParagraphs()) {
                String text = para.getText();
                lines.add(text == null ? "" : text);
            }

        } catch (Exception e) {
            System.err.println("QuestionParser: could not read file — "
                    + e.getMessage());
        }

        return lines;
    }
}
