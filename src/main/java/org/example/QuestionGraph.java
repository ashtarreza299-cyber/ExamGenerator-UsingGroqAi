package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;

public class QuestionGraph extends JPanel implements ExamComponent{

    private String[] labels = {
            "MCQ", "True/False", "Descriptive", "Fill Blank"
    };


    private List<Integer> values = new ArrayList<>();

    private final Color[] colors = {
            new Color(52, 152, 219),
            new Color(46, 204, 113),
            new Color(241, 196, 15),
            new Color(231, 76, 60)
    };

    private Model model;

    private int total = 0;

    public QuestionGraph(Model model) {

        this.model = model;

        setBounds(0, 30, model.getDistributionBoxWidth(), model.getDistributionBoxHeight());
        setOpaque(false);
        setLayout(null);
    }

    private void calculateValues() {

        values.clear();

        int numberOfMCQ = 0;
        int numberOfTrueFalse = 0;
        int numberOfDescriptive = 0;
        int numberOfFillBlank = 0;

        for (Question q : model.getQuestions()) {
            if (q instanceof MultipleChoiceQuestion) numberOfMCQ++;
            else if (q instanceof TrueFalseQuestion) numberOfTrueFalse++;
            else if (q instanceof BlankSpaceQuestion) numberOfFillBlank++;
            else if (q instanceof DesceiptiveQuestion) numberOfDescriptive++;
        }

        values.add(numberOfMCQ);
        values.add(numberOfTrueFalse);
        values.add(numberOfDescriptive);
        values.add(numberOfFillBlank);

        total = 0;
        for (int v : values) total += v;
        if (total == 0) total = 0; // keep zero
    }

    public void drawColoredGraph(Graphics2D g2, int size, int x, int y) {

        int startAngle = 0;

        for(int i = 0; i < values.size(); i++) {

            int arcAngle = (int) Math.round((values.get(i) * 360.0) / total);
            g2.setColor(colors[i]);
            g2.fill(new Arc2D.Double(x, y, size, size, startAngle, arcAngle, Arc2D.PIE));
            startAngle += arcAngle;

        }

    }

    public void drawInnerCircle(Graphics2D g2, int size, int x, int y) {

        int innerSize = size/2;

        g2.setColor(Color.WHITE);
        g2.fillOval(x + size/4, y + size/4, innerSize, innerSize);

    }

    public void drawInnerText(Graphics2D g2, int x, int y) {

        //inner Text
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 28));

        String text = String.valueOf(total);

        FontMetrics fm = g2.getFontMetrics();

        g2.drawString(text, x * 5 + fm.stringWidth(text) + 10, y * 3 + 10);

        g2.setFont(new Font("ARIAL", Font.BOLD, 16));
        String subtitle = "Total";
        fm = g2.getFontMetrics();

        g2.drawString(subtitle,
                x * 5 + fm.stringWidth(text) + 10,
                y * 3 - 15
        );

    }

    public void drawColorNotation(Graphics2D g2,  int size) {

        int offsetY = model.getDistributionBoxHeight()/4;

        for(int i = 0; i < colors.length; i++) {
            colorNotation(g2, colors[i], size + 20, offsetY);
            offsetY += 40;
        }

    }

    public void drawQuestionTypes(Graphics2D g2, int size) {

        int offsetY = model.getDistributionBoxHeight()/4 + 15;
        for(int i = 0; i < labels.length; i++) {

            FontMetrics fm = g2.getFontMetrics();

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.drawString(labels[i], size + 50, offsetY);
            g2.drawString(String.valueOf(values.get(i)), size + 150, offsetY);
            offsetY += 40;
        }

    }

    public void colorNotation(Graphics2D g2, Color color, int offsetX, int offsetY) {

        g2.setColor(color);
        g2.fillRoundRect(offsetX, offsetY, 20, 20, 5, 5);
    };

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        // Recalculate values on each paint so the graph reflects current model.questions
        calculateValues();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = 300;
        int height = 300;

        int size = Math.min(width, height) - 100;

        int x =  (width - size) / 6;
        int y = (height - size) / 2;

        if (total > 0) drawColoredGraph(g2, size, x, y);

        drawInnerCircle(g2, size, x, y);

        drawInnerText(g2, x, y);

        drawColorNotation(g2, size);

        drawQuestionTypes(g2, size);

    }

}
