package org.example.model;


public class BlankSpaceQuestion extends Question {

    private String question;

    public BlankSpaceQuestion(String question) {

        super(question);
        this.question = question;
    }

    @Override
    public String format() {

        return question;
    }
}