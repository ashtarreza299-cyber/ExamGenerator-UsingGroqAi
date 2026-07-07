package org.example;

import javax.swing.*;
import java.awt.*;

public class QuestionDistributionPanel extends JPanel{

    private JLabel title = new JLabel("Question Type Distribution");

    private Model model;

    public  QuestionDistributionPanel(Model model) {

        this.model = model;
        setDistributionBox();
        setProperties();
        add(new QuestionGraph(model));
        add(title);

    }

    public void createPanel(Graphics2D g2) {
        g2.setColor(new Color(230, 230, 230));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
    }

    private void setDistributionBox() {
        model.setDistributionBoxX(model.getSideBarWidth() + model.getDashBoardWidth() + 20);
        model.setDistributionBoxWidth(model.getPanelWidth() - model.getDistributionBoxX() - 10);
        model.setDistributionBoxHeight(300);
        setBounds(
                model.getDistributionBoxX(),
                0,
                model.getDistributionBoxWidth(),
                model.getDistributionBoxHeight()
        );
    }

    private void setProperties() {
        setBackground(Color.white);
        setLayout(null);
        setTitleLabelFont(title);
        title.setBounds(10, 10, getWidth(), 50);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D  g2 = (Graphics2D) g;

        createPanel(g2);

    }

    public void setTitleLabelFont(JLabel title){
        title.setFont(new Font("Arial", Font.BOLD, 20));
    }


}
