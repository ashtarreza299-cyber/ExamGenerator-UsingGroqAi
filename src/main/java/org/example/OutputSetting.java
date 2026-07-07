
package org.example;

import javax.swing.*;
import java.awt.*;

public class OutputSetting extends JPanel implements DashBoardInnerPanel {

    private final JLabel title = new JLabel("4.Output Setting");
    private final JLabel outputFolderLabel = new JLabel("Output Folder");
    private final JLabel fileNamingLabel = new JLabel("File Naming");
    private final JLabel startNumberLabel = new JLabel("Start Number From");

    Model model;
    public OutputSetting(Model model) {

        this.model = model;

        setBounds(
                model.getQuestionTypePanelWidth() + 30,
                model.getExamSettingBoxHeight() + model.getExamSettingBoxY() + 10,
                model.getDashBoardWidth() - model.getQuestionTypePanelWidth() - 50,
                model.getQuestionTypePanelHeight());

        setLayout(null);
        addComponents();
        setLabels();
    }

    public void createPanel(Graphics2D g2) {

        g2.setColor(new Color(217, 217, 217));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
    }

    public void addComponents() {
        add(title);
        add(outputFolderLabel);
        add(fileNamingLabel);
        add(startNumberLabel);
    }

    public void setLabels() {

        setTitleLabelFont(title);
        title.setBounds(10, 10, getWidth(), 50);

        setTextLabelFont(outputFolderLabel);
        outputFolderLabel.setBounds(10, 50, getWidth(), 50);

        setTextLabelFont(fileNamingLabel);
        fileNamingLabel.setBounds(10, 150, getWidth(), 50);

        setTextLabelFont(startNumberLabel);
        startNumberLabel.setBounds(10, 200, getWidth(), 50);

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
