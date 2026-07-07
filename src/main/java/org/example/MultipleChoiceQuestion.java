package org.example;

import java.util.Collections;
import java.util.List;

public class MultipleChoiceQuestion extends Question {

    String question;
    List<String> options;
    Model model;

     public MultipleChoiceQuestion(String question,  List<String> options, Model model) {

         super(question);
         this.model = model;
         this.question = question;
         this.options = options;
     }

    @Override
    public String format() {

         StringBuilder builder = new StringBuilder();

         builder.append(question + "\n");

         char letter = 'A';
         builder.append(" ");
         for(String option: options){
             builder.append(letter++).append(") ").append(option).append("\n");
         }

         return builder.toString();
    }

    public void shuffleOption() {

         if(model.isOptionShufflingOn())
            Collections.shuffle(options);
    }

}
