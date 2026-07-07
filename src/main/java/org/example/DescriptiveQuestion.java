package org.example;

public class DescriptiveQuestion extends Question {

    private String question;

    public DescriptiveQuestion(String question) {

        super(question);
        this.question=question;
    }

    @Override
    public String format() {
        return question;
    }
}
