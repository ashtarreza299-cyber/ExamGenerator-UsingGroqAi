package org.example;

import javax.swing.*;
import java.awt.*;

public class ExamSettingPanel extends JPanel implements DashBoardInnerPanel {

    private final JSpinner[] spinners = {

            new JSpinner(new SpinnerNumberModel()),
            new JSpinner(new SpinnerNumberModel()),
    };

    private final ToggleSwitch[] ts = {

            new ToggleSwitch(),
            new ToggleSwitch(),
            new ToggleSwitch()
    };

    private final JLabel examSettingLabel = new JLabel("2. Exam Setting");

    private final JLabel[] examSettingLabels = {

            new JLabel("Number of students / Exam"),
            new JLabel("Number of questions per exam"),
            new JLabel("Shuffle question"),
            new JLabel("Shuffle options in MCQS"),
            new JLabel("Include answer key")
    };

    Model model;


    public ExamSettingPanel(Model model) {

        this.model = model;

        setUpPanel();
        setLayout(null);
        addComponent();

    }

    public void addComponent() {

        add(examSettingLabel);

        for(JLabel label: examSettingLabels)
            add(label);

        for(JSpinner spinner: spinners)
            add(spinner);

        for(ToggleSwitch t: ts) {
            add(t);
        }

    }

    public void setUpPanel() {

        setExamSettingBoxLabels();
        setNumberSpinnerAndToggleSwitch();



        setBounds(
                model.getExamSettingBoxX(),
                model.getExamSettingBoxY(),
                model.getExamSettingBoxWidth(),
                model.getExamSettingBoxHeight()
        );
    }

    public void createPanel(Graphics2D g2){

        g2.setColor(new Color(217, 217, 217));//230, 230, 230

        g2.fillRoundRect(0 , 0,
                model.getExamSettingBoxWidth(), model.getExamSettingBoxHeight(), 10, 10);

    }

    public void setExamSettingBoxLabels() {

        setTitleLabelFont(examSettingLabel);
        examSettingLabel.setBounds(20, 10, 250, 40);

        int offsetY = 50;
        for(JLabel label: examSettingLabels){

            setTextLabelFont(label);
            // bounds must be relative to this panel's origin (0,0)
            // use the model width for sizing but use local x/y coordinates
            label.setBounds(20, offsetY,  model.getExamSettingBoxWidth() - 40, 30);
            offsetY += 40;

        }

    }

    public void setNumberSpinnerAndToggleSwitch() {

        int offsetY = 50;
        for(JSpinner spinner : spinners) {

            // position spinners relative to this panel: place them near the right edge
            spinner.setBounds(
                    model.getExamSettingBoxWidth() - 100,
                    offsetY, 80, 30);
            offsetY += 40;
        }

        for(ToggleSwitch t: ts) {
            t.setBounds(
                    model.getExamSettingBoxWidth() - 100,
                    offsetY, 50, 30);
            offsetY += 40;
        }

    }

    public void setTitleLabelFont(JLabel title){
        title.setFont(new Font("Arial", Font.BOLD, 20));
    }

    public void setTextLabelFont(JLabel text){
        text.setFont(new Font("Arial", Font.PLAIN, 16));
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 =  (Graphics2D) g;

        createPanel(g2);

    }


}
