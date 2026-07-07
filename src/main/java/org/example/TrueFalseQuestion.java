package org.example;

public class TrueFalseQuestion extends Question {

    private String question;
    public TrueFalseQuestion(String question) {
        super(question);
        this.question = question;
    }

    @Override
    public String format() {

        StringBuilder builder = new StringBuilder();

        builder.append(question).append("\n").append("True / False");

        return builder.toString();
    }

}
