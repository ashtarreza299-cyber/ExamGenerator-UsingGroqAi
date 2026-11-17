package org.example.model;


import java.util.ArrayList;
import java.util.List;


public class ExamStructure {

    /** Lines that appear before the first question part (university info, name fields, etc.) */
    private List<String> headerLines = new ArrayList<>();

    /** The exam parts in order (Part One: MCQ, Part Two: Descriptive, etc.) */
    private List<ExamPart> parts = new ArrayList<>();

    public static class ExamPart {

        private String title;             // e.g. "Part One: Multiple Choice Questions"
        private List<ExamQuestion> questions = new ArrayList<>();

        public ExamPart(String title) { this.title = title; }

        public String getTitle() { return title; }
        public List<ExamQuestion> getQuestions() { return questions; }
        public void addQuestion(ExamQuestion q) { questions.add(q); }
    }

    public static class ExamQuestion {

        private String stem;              // The question text
        private List<String> options;     // For MCQ: A B C D options; empty for others
        private String type;             // "MCQ", "TF", "BLANK", "DESCRIPTIVE"

        public ExamQuestion(String stem, List<String> options, String type) {
            this.stem    = stem;
            this.options = options != null ? options : new ArrayList<>();
            this.type    = type;
        }

        public String getStem()          { return stem; }
        public List<String> getOptions() { return options; }
        public String getType()          { return type; }
        public boolean isMCQ()           { return "MCQ".equals(type); }
    }

    // ----------------------------------------------------------------
    // Getters / setters
    // ----------------------------------------------------------------

    public List<String> getHeaderLines()  { return headerLines; }
    public List<ExamPart> getParts()      { return parts; }
    public void addHeaderLine(String l)   { headerLines.add(l); }
    public void addPart(ExamPart p)       { parts.add(p); }

    /** Total question count across all parts */
    public int totalQuestions() {
        return parts.stream().mapToInt(p -> p.getQuestions().size()).sum();
    }
}
