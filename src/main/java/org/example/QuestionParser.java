package org.example;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a .docx file into a list of Question objects.
 *
 * Strategy
 * --------
 * Questions are separated by blank lines (or lines that are purely whitespace).
 * Each non-empty block is handed to QuestionClassifier, which decides the type
 * automatically — no [M] / [D] / [T] / [B] tags are needed.
 *
 * Word document format expected:
 * ─────────────────────────────
 * What is the capital of France?
 * A) London
 * B) Berlin
 * C) Paris
 * D) Madrid
 *                          ← blank paragraph separates questions
 * The sky is blue. (True or False)
 *                          ← blank paragraph
 * The ________ is the powerhouse of the cell.
 *                          ← blank paragraph
 * Explain the water cycle in detail.
 */
public class QuestionParser {

    /**
     * Parse every question from the given .docx file.
     *
     * @param file   the Word document to read
     * @param model  shared model (passed through to MultipleChoiceQuestion)
     * @return       list of classified Question objects, never null
     */
    public static List<Question> parse(File file, Model model) {

        List<Question> questions = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {

            List<String> blockLines = new ArrayList<>();

            for (XWPFParagraph para : doc.getParagraphs()) {

                String line = para.getText();

                if (line == null || line.isBlank()) {
                    // Blank paragraph = end of one question block
                    flushBlock(blockLines, questions, model);
                    blockLines.clear();
                } else {
                    blockLines.add(line);
                }
            }

            // Flush the last block (file may not end with a blank line)
            flushBlock(blockLines, questions, model);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return questions;
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Join the accumulated lines into one raw text block, classify it,
     * and add the resulting Question to the output list.
     */
    private static void flushBlock(List<String> lines,
                                   List<Question> out,
                                   Model model) {
        if (lines.isEmpty()) return;

        String rawBlock = String.join("\n", lines).trim();
        if (rawBlock.isBlank()) return;

        Question q = QuestionClassifier.classify(rawBlock, model);
        if (q != null) out.add(q);
    }
}
