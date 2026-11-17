package org.example;

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
import org.example.service.ExamGenerator;
import org.example.service.ExamStructureParser;
import org.example.service.GenerateExam;
import org.example.service.PerplexityClassifier;
import org.example.service.QuestionClassifier;
import org.example.service.QuestionExtractor;
import org.example.service.QuestionParser;
import org.example.service.QuestionShuffler;
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



}