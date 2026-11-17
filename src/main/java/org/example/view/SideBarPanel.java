package org.example.view;

import org.example.model.BlankSpaceQuestion;
import org.example.model.DescriptiveQuestion;
import org.example.model.ExamStructure;
import org.example.model.Model;
import org.example.model.ModelListener;
import org.example.model.MultipleChoiceQuestion;
import org.example.model.Question;
import org.example.model.TrueFalseQuestion;
import org.example.view.DashBoardInnerPanel;
import org.example.view.DashBoardPanel;
import org.example.view.ExamSettingPanel;
import org.example.view.LoadQuestionPanel;
import org.example.view.OutputSetting;
import org.example.view.ProgramViewPanel;
import org.example.view.QuestionDistributionPanel;
import org.example.view.QuestionPreviewPanel;
import org.example.view.QuestionPreviewer;
import org.example.view.QuestionTypePanel;
import org.example.view.SideBarPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SideBarPanel extends ProgramViewPanel implements ModelListener {

    private Model model;

    private JLabel title = new JLabel("Exam Generator");

    // Question statistics
    private JLabel statsTitle = new JLabel("Question Statistics");
    private JLabel totalQuestions = new JLabel();
    private JLabel mcqCount = new JLabel();
    private JLabel tfCount = new JLabel();
    private JLabel blankCount = new JLabel();
    private JLabel desCount = new JLabel();

    // Exam settings
    private JLabel settingsTitle = new JLabel("Exam Settings");
    private JLabel students = new JLabel();
    private JLabel questionsPerExam = new JLabel();
    private JLabel shuffleQuestions = new JLabel();
    private JLabel shuffleOptions = new JLabel();
    private JLabel answerKey = new JLabel();

    private JLabel shuffleMCQ = new JLabel();
    private JLabel shuffleDes = new JLabel();
    private JLabel shuffleBlank = new JLabel();
    private JLabel shuffleTrueFalse = new JLabel();


    // Output
    private JLabel outputTitle = new JLabel("Output");
    private JLabel fileName = new JLabel();
    private JLabel outputFolder = new JLabel();

    // Status
    private JLabel statusTitle = new JLabel("Status");
    private JLabel status = new JLabel();

    public SideBarPanel(Model model) {

        this.model = model;

        createPanel();
        setLayout(null);

        initializeLabels();

        // register for model changes and initialize view
        model.addModelListener(this);
        // initial refresh
        refreshInfo();
    }

    private void initializeLabels() {

        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBounds(15, 20, 220, 30);
        add(title);

        int y = 80;

        // Question Statistics
        configureSectionTitle(statsTitle, y);
        y += 35;

        configureInfoLabel(totalQuestions, y);
        y += 25;

        configureInfoLabel(mcqCount, y);
        y += 25;

        configureInfoLabel(tfCount, y);
        y += 25;

        configureInfoLabel(blankCount, y);
        y += 25;

        configureInfoLabel(desCount, y);
        y += 45;

        // Exam Settings
        configureSectionTitle(settingsTitle, y);
        y += 35;

        configureInfoLabel(students, y);
        y += 25;

        configureInfoLabel(questionsPerExam, y);
        y += 25;


        configureInfoLabel(answerKey, y);
        y += 25;

        configureInfoLabel(shuffleOptions, y);
        y += 25;

        configureInfoLabel(shuffleQuestions, y);
        y += 25;


        configureInfoLabel(shuffleMCQ, y);
        y += 25;

        configureInfoLabel(shuffleDes, y);
        y += 25;

        configureInfoLabel(shuffleBlank, y);
        y += 25;

        configureInfoLabel(shuffleTrueFalse, y);
        y += 25;


        /// ///////////////////////////////

        // Output
        configureSectionTitle(outputTitle, y);
        y += 35;

        configureInfoLabel(fileName, y);
        y += 25;

        configureInfoLabel(outputFolder, y);
        y += 45;

        // Status
        configureSectionTitle(statusTitle, y);
        y += 35;

        configureInfoLabel(status, y);
    }

    private void configureSectionTitle(JLabel label, int y) {

        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        label.setBounds(15, y, 220, 25);

        add(label);
    }

    private void configureInfoLabel(JLabel label, int y) {

        label.setFont(new Font("Arial", Font.PLAIN, 13));
        label.setForeground(new Color(220, 220, 220));
        label.setBounds(20, y, 220, 20);

        add(label);
    }

    public void refreshInfo() {

        int mcq = 0;
        int tf = 0;
        int blank = 0;
        int des = 0;

        for (Question q : model.getQuestions()) {

            if (q instanceof MultipleChoiceQuestion)
                mcq++;

            else if (q instanceof TrueFalseQuestion)
                tf++;

            else if (q instanceof BlankSpaceQuestion)
                blank++;

            else if (q instanceof DescriptiveQuestion)
                des++;
        }

        totalQuestions.setText("Total Questions: " + model.getQuestions().size());
        mcqCount.setText("MCQ: " + mcq);
        tfCount.setText("True / False: " + tf);
        blankCount.setText("Fill Blank: " + blank);
        desCount.setText("Descriptive: " + des);

        students.setText("Students: " + model.getNumberOfStudents());
        questionsPerExam.setText("Questions/Exam: " + model.getNumberOfQuestion());

        answerKey.setText(
                "Answer Key: " +
                        (model.isAnswerKeyIncluded() ? "ON" : "OFF"));

        shuffleOptions.setText(
                "Shuffle Options: " +
                        (model.isOptionShufflingOn() ? "ON" : "OFF"));

        shuffleQuestions.setText(
                "Shuffle Questions: " +
                        (model.isShufflingOn() ? "ON" : "OFF"));

        shuffleMCQ.setText(
                "Shuffle MCQ: " +
                        (model.isMcqShuffled() ? "ON" : "OFF"));

        shuffleDes.setText(
                "Shuffle descriptive: " +
                        (model.isDesShuffled() ? "ON" : "OFF"));

        shuffleBlank.setText(
                "Shuffle fill blank space: " +
                        (model.isBlankSpaceShuffled() ? "ON" : "OFF"));

        shuffleTrueFalse.setText(
                "Shuffle true false: " +
                        (model.isTrueFalseShuffled() ? "ON" : "OFF"));


        if(!model.isShufflingOn()){

            shuffleMCQ.setText("Shuffle MCQ: OFF");
            shuffleDes.setText("Shuffle descriptive: OFF");
            shuffleBlank.setText("Shuffle fill blank space: OFF");
            shuffleTrueFalse.setText("Shuffle true false: OFF");
        }




        fileName.setText(
                "File: " +
                        (model.getFileNaming() == null ||
                                model.getFileNaming().isBlank()
                                ? "-"
                                : model.getFileNaming()));

        File folder = model.getOutputFolder();

        outputFolder.setText(
                "Folder: " +
                        (folder == null ? "-" : folder.getName()));

        boolean ready =
                model.getQuestions().size() > 0
                        && folder != null
                        && model.getFileNaming() != null
                        && !model.getFileNaming().isBlank()
                        && model.getNumberOfStudents() != 0
                        && model.getNumberOfQuestion() != 0
                ;

        status.setText(
                ready
                        ? "Ready To Generate"
                        : "Configuration Incomplete");

        if(ready)
            status.setForeground(Color.GREEN);
        else
            status.setForeground(Color.RED);
    }

    @Override
    public void modelChanged() {
        // Ensure UI update on EDT
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            refreshInfo();
        } else {
            javax.swing.SwingUtilities.invokeLater(this::refreshInfo);
        }
    }

    @Override
    public void createPanel() {

        setBackground(new Color(0, 0, 102));

        setBounds(
                0,
                0,
                model.getSideBarWidth(),
                model.getPanelHeight()
        );
    }
}