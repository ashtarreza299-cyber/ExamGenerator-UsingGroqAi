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

// ---------------------------------------------------------------
// Replace ONLY the generateExam() method in your DashBoardPanel.java
// with this code. Everything else in DashBoardPanel stays the same.
// ---------------------------------------------------------------

import org.apache.poi.xwpf.usermodel.*;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenerateExam {

    /**
     * Call this from DashBoardPanel.generateExam() replacing all existing logic.
     * Pass `this` (the DashBoardPanel) as the `parent` parameter.
     */
    public static void generate(JPanel parent, Model model) {

        // ── Safety checks ──────────────────────────────────────────
        if (model.getOutputFolder() == null) {
            JOptionPane.showMessageDialog(parent, "Please select output folder!");
            return;
        }

        if (model.getSelectedFile() == null) {
            JOptionPane.showMessageDialog(parent, "Please load a Word file first!");
            return;
        }

        String apiKey = ApiKeyManager.loadKey();
        if (apiKey == null) {
            JOptionPane.showMessageDialog(parent,
                    "Perplexity API key not found!\nPlease restart the app to set up your key.");
            return;
        }

        int studentCount = model.getNumberOfStudents();
        if (studentCount <= 0) {
            JOptionPane.showMessageDialog(parent, "Please set number of students!");
            return;
        }

        // ── Parse exam structure via Perplexity ──────────────────────────
        JOptionPane.showMessageDialog(parent,
                "Analyzing exam structure with AI...\nThis may take a few seconds.");

        ExamStructure structure = ExamStructureParser.parse(model.getSelectedFile(), apiKey);

        if (structure == null || structure.getParts().isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Could not parse exam structure. Please check your Word file.");
            return;
        }

        // ── Generate Word document ─────────────────────────────────
        try (XWPFDocument doc = new XWPFDocument()) {

            for (int s = 0; s < studentCount; s++) {

                // Student label
                addStyledParagraph(doc, "Student " + (s + 1),
                        ParagraphAlignment.CENTER, 16, true);

                // Header lines (university name, course, date, name fields, etc.)
                for (String headerLine : structure.getHeaderLines()) {
                    addStyledParagraph(doc, headerLine,
                            ParagraphAlignment.CENTER, 12, false);
                }

                addEmptyLine(doc);

                // Parts — shuffle questions WITHIN each part only
                for (ExamStructure.ExamPart part : structure.getParts()) {

                    // Part title
                    if (!part.getTitle().isBlank()) {
                        addStyledParagraph(doc, part.getTitle(),
                                ParagraphAlignment.LEFT, 13, true);
                    }

                    // Copy questions and shuffle within this part only
                    List<ExamStructure.ExamQuestion> questions =
                            new ArrayList<>(part.getQuestions());

                    if (model.isShufflingOn()) {
                        Collections.shuffle(questions);
                    }

                    // Write questions
                    int num = 1;
                    for (ExamStructure.ExamQuestion q : questions) {

                        // Question stem with number
                        addStyledParagraph(doc,
                                num++ + ". " + q.getStem(),
                                ParagraphAlignment.LEFT, 12, false);

                        // MCQ options
                        if (q.isMCQ() && !q.getOptions().isEmpty()) {

                            List<String> options = new ArrayList<>(q.getOptions());

                            // Shuffle options if enabled
                            if (model.isOptionShufflingOn()) {
                                Collections.shuffle(options);
                            }

                            char letter = 'A';
                            for (String option : options) {
                                addStyledParagraph(doc,
                                        "    " + letter++ + ") " + option,
                                        ParagraphAlignment.LEFT, 12, false);
                            }
                        }

                        // True/False — add True / False lines
                        if ("TF".equals(q.getType())) {
                            addStyledParagraph(doc, "    A) True",
                                    ParagraphAlignment.LEFT, 12, false);
                            addStyledParagraph(doc, "    B) False",
                                    ParagraphAlignment.LEFT, 12, false);
                        }

                        addEmptyLine(doc);
                    }

                    addEmptyLine(doc);
                }

                // Page break between students (not after the last one)
                if (s < studentCount - 1) {
                    XWPFParagraph pageBreak = doc.createParagraph();
                    pageBreak.setPageBreak(true);
                }
            }

            // ── Save file ──────────────────────────────────────────
            String fileName = (model.getFileNaming() != null
                    && !model.getFileNaming().isBlank())
                    ? model.getFileNaming()
                    : "Exam";

            int startNum = model.getStartNumber();
            String finalName = fileName + "_" + startNum + ".docx";
            File outFile = new File(model.getOutputFolder(), finalName);

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                doc.write(fos);
            }

            JOptionPane.showMessageDialog(parent,
                    "✓ Exam generated successfully!\n"
                    + studentCount + " student exams saved to:\n"
                    + outFile.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error: " + ex.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Word document helpers
    // ----------------------------------------------------------------

    private static void addStyledParagraph(XWPFDocument doc,
                                            String text,
                                            ParagraphAlignment alignment,
                                            int fontSize,
                                            boolean bold) {
        XWPFParagraph para = doc.createParagraph();
        para.setAlignment(alignment);
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setFontSize(fontSize);
        run.setBold(bold);
        run.setFontFamily("Arial");
    }

    private static void addEmptyLine(XWPFDocument doc) {
        doc.createParagraph();
    }
}
