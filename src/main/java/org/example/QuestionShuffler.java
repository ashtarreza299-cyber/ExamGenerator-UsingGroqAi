package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class QuestionShuffler {

    public static ArrayList<Question> shuffleQuestion(
            List<Question> questions,
            Model model
    ) {

        List<Question> mcq = new ArrayList<>();
        List<Question> tf = new ArrayList<>();
        List<Question> blank = new ArrayList<>();
        List<Question> des = new ArrayList<>();

        // دسته بندی
        for (Question q : questions) {

            if (q instanceof MultipleChoiceQuestion) {

                if (model.isOptionShufflingOn()) {
                    ((MultipleChoiceQuestion) q).shuffleOption();
                }

                mcq.add(q);

            } else if (q instanceof TrueFalseQuestion) {

                tf.add(q);

            } else if (q instanceof BlankSpaceQuestion) {

                blank.add(q);

            } else if (q instanceof DesceiptiveQuestion) {

                des.add(q);
            }
        }

        // شافل داخل هر دسته
        if (model.isShufflingOn()) {

            Collections.shuffle(mcq);
            Collections.shuffle(tf);
            Collections.shuffle(blank);
            Collections.shuffle(des);

        }

        ArrayList<Question> result = new ArrayList<>();

        // ترتیب بخش ها حفظ می شود
        result.addAll(mcq);
        result.addAll(tf);
        result.addAll(blank);
        result.addAll(des);

        return result;
    }
}