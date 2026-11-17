package org.example.service;

import org.example.model.BlankSpaceQuestion;
import org.example.model.DescriptiveQuestion;
import org.example.model.MultipleChoiceQuestion;
import org.example.model.Question;
import org.example.model.TrueFalseQuestion;
import org.example.model.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds the final ordered exam question list from a pool of questions,
 * applying the shuffle options stored in the {@link Model}.
 * (Moved verbatim from Main.buildExam during restructuring.)
 */
public class ExamBuilder {

    public static List<Question> buildExam(List<Question> questions, Model model) {
        List<Question> mcqList = new ArrayList<>();
        List<Question> tfList = new ArrayList<>();
        List<Question> blankList = new ArrayList<>();
        List<Question> desList = new ArrayList<>();


        for (Question question : questions) {
            if (question instanceof MultipleChoiceQuestion) {
                mcqList.add(question);
            } else if (question instanceof TrueFalseQuestion) {
                tfList.add(question);
            } else if (question instanceof BlankSpaceQuestion) {
                blankList.add(question);
            } else if (question instanceof DescriptiveQuestion) {
                desList.add(question);
            }
        }

        if (model.isShufflingOn()) {
            Collections.shuffle(mcqList);
            Collections.shuffle(tfList);
            Collections.shuffle(blankList);
            Collections.shuffle(desList);
        }

        if (model.isOptionShufflingOn()) {
            for (Question question : mcqList) {
                if (question instanceof MultipleChoiceQuestion) {
                    ((MultipleChoiceQuestion) question).shuffleOption();
                }
            }
        }

        List<Question> finalExamList = new ArrayList<>();
        finalExamList.addAll(mcqList);
        finalExamList.addAll(tfList);
        finalExamList.addAll(blankList);
        finalExamList.addAll(desList);

        return finalExamList;
    }
}
