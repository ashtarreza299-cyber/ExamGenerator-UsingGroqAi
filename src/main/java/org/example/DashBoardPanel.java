package org.example;

import org.example.Image.LoadQuestionPanel;

import javax.swing.*;
import java.awt.*;

public class DashBoardPanel extends ProgramViewPanel {

    private final JLabel dashboardLabel = new JLabel("Dashboard");
    private final JLabel desLabel = new JLabel("Create and manage exams easily");

    private final ImageIcon generateIcon = new ImageIcon("C:\\Users\\ashta\\IdeaProjects\\ShuffleQuestionGUI\\src\\main\\java\\org\\example\\Image\\4.png");
    private final ImageIcon scaledGenerateIcon = new ImageIcon(generateIcon.getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT));
    private final MyButton generateButton = new MyButton();

    Model model;
    public DashBoardPanel(Model model) {

        this.model = model;
        createPanel();

        model.setExamSettingBoxX(model.getDashBoardWidth() - model.getExamSettingBoxWidth() - 20);
        model.setExamSettingBoxY(model.getLoadQuestionBoxY());

        setLabels();
        setGenerateButton();

        setLayout(null);
        addComponent();
    }

    public void addComponent() {

        add(dashboardLabel);
        add(desLabel);
        add(generateButton);
        add(new LoadQuestionPanel(model));
        add(new ExamSettingPanel(model));
        add(new QuestionTypePanel(model));
        add(new OutputSetting(model));
    }

    @Override
    public void createPanel(){
        setBounds(model.getSideBarWidth() + 10, 0, model.getDashBoardWidth(), model.getPanelHeight());
    }

    public void setTitleLabelFont(JLabel title){
        title.setFont(new Font("Arial", Font.BOLD, 20));
    }

    public void setTextLabelFont(JLabel text){
        text.setFont(new Font("Arial", Font.PLAIN, 16));
    }

    public void setLabels() {

        setTitleLabelFont(dashboardLabel);
        dashboardLabel.setBounds(20, 20, 200, 50);

        setTextLabelFont(desLabel);
        desLabel.setBounds(20, 50, 200, 50);

    }

    public void setGenerateButton() {

        generateButton.setIcon(scaledGenerateIcon);
        generateButton.setText("Generate Exam");
        generateButton.setBounds(model.getDashBoardWidth() - model.getSideBarButtonsWidth() - 20, 30, model.getSideBarButtonsWidth(), 50);
    }

    public void setDashboardBounds(Graphics2D g2){

        g2.setColor(new Color(230, 230, 230));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        setDashboardBounds(g2);

    }


}
