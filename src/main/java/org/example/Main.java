package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.UIManager;

public class Main extends JFrame {

    Model model;

    private ProgramViewPanel sideBarPanel;
    private ProgramViewPanel dashBoardPanel;
    private QuestionDistributionPanel questionDistributionPanel;
    private QuestionPreviewPanel  questionPreviewPanel;

    public Main() {

        String apiKey = ApiKeyManager.initialize();
        QuestionClassifier.setApiKey(apiKey);

        model = new Model();
        setUpFrame();
        createPanels();
        addComponents();

    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                        "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame frame = new Main();
            frame.setVisible(true);
        });


    }

    public void setUpFrame(){


        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        setIconImage(
                new ImageIcon(getClass().getResource("/Image/ProgramIcon.png")).getImage()
        );

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        model.setPanelWidth(screen.width);
        model.setPanelHeight(screen.height);

    }

    public void createPanels() {
        sideBarPanel = new SideBarPanel(model);
        dashBoardPanel = new DashBoardPanel(model);
        questionDistributionPanel = new QuestionDistributionPanel(model);
        questionPreviewPanel = new QuestionPreviewPanel(model);
    }

    public void addComponents() {

        add(sideBarPanel);
        add(dashBoardPanel);
        add(questionDistributionPanel);
        add(questionPreviewPanel);

    }

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