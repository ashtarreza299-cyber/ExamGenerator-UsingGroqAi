package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


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

        ExamSettingPanel settingPanel = new ExamSettingPanel(model);

        settingPanel.setPreferredSize(
                new Dimension(
                        model.getExamSettingBoxWidth(),
                        400
                )
        );

        JScrollPane scrollPane = new JScrollPane(settingPanel);

        scrollPane.setBounds(
                model.getExamSettingBoxX(),
                model.getExamSettingBoxY(),
                model.getExamSettingBoxWidth(),
                model.getExamSettingBoxHeight())
        ;

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(217, 217, 217));
        scrollPane.getViewport().setBackground(new Color(217, 217, 217));

        add(dashboardLabel);
        add(desLabel);
        add(generateButton);
        add(new LoadQuestionPanel(model));
        add(scrollPane);
        add(new QuestionTypePanel(model));
        add(new OutputSetting(model));
    }

    @Override
    public void createPanel(){

        model.setDashBoardX(model.getSideBarWidth() + 10);
        model.setDashBoardY(0);

        setBounds(model.getDashBoardX(), model.getDashBoardY(), model.getDashBoardWidth(), model.getPanelHeight());
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


        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(model.isShufflingOn()){
                    if(model.isMcqShuffled() || model.isBlankSpaceShuffled()
                            || model.isDesShuffled() || model.isBlankSpaceShuffled()){
                        generateExam();
                    }else{
                        JOptionPane.showMessageDialog(null, "Since the Shuffle Question Option is selected\nAt least on shuffling options must be selected to generate the exam",
                                "Enable Question Shuffle Option", JOptionPane.INFORMATION_MESSAGE);
                    }
                }else
                    generateExam();
            }
        });


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

    private void generateExam() {
        ExamGenerator.generate(this, model);
    }



}
