package org.example.view.component;

import org.example.model.BlankSpaceQuestion;
import org.example.model.DescriptiveQuestion;
import org.example.model.ExamStructure;
import org.example.model.Model;
import org.example.model.ModelListener;
import org.example.model.MultipleChoiceQuestion;
import org.example.model.Question;
import org.example.model.TrueFalseQuestion;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;

public class QuestionGraph extends JPanel implements ExamComponent {

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

        int numberOfMCQ         = 0;
        int numberOfTrueFalse   = 0;
        int numberOfDescriptive = 0;
        int numberOfFillBlank   = 0;

        for (Question q : model.getQuestions()) {
            if      (q instanceof MultipleChoiceQuestion) numberOfMCQ++;
            else if (q instanceof TrueFalseQuestion)      numberOfTrueFalse++;
            else if (q instanceof BlankSpaceQuestion)     numberOfFillBlank++;
            else if (q instanceof DescriptiveQuestion)    numberOfDescriptive++;
        }

        values.add(numberOfMCQ);
        values.add(numberOfTrueFalse);
        values.add(numberOfDescriptive);
        values.add(numberOfFillBlank);

        total = 0;
        for (int v : values) total += v;
    }

    public void drawColoredGraph(Graphics2D g2, int size, int x, int y) {

        int startAngle = 0;

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == 0) continue;
            int arcAngle = (int) Math.round((values.get(i) * 360.0) / total);
            g2.setColor(colors[i]);
            g2.fill(new Arc2D.Double(x, y, size, size, startAngle, arcAngle, Arc2D.PIE));
            startAngle += arcAngle;
        }
    }

    public void drawInnerCircle(Graphics2D g2, int size, int x, int y) {

        int innerSize = size / 2;
        g2.setColor(Color.WHITE);
        g2.fillOval(x + size / 4, y + size / 4, innerSize, innerSize);
    }

    public void drawInnerText(Graphics2D g2, int size, int x, int y) {

        // Center of the donut hole
        int centerX = x + size / 2;
        int centerY = y + size / 2;

        // Draw total number — centered
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fmBig = g2.getFontMetrics();
        String totalText = String.valueOf(total);
        int totalW = fmBig.stringWidth(totalText);
        int totalH = fmBig.getAscent();

        // Draw "Total" label above the number
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fmSmall = g2.getFontMetrics();
        String subtitle = "Total";
        int subtitleW = fmSmall.stringWidth(subtitle);

        // Stack: subtitle on top, number below — both centered horizontally
        int gap = 4;
        int blockHeight = fmSmall.getHeight() + gap + fmBig.getHeight();
        int startY = centerY - blockHeight / 2 + fmSmall.getAscent();

        // Draw subtitle
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString(subtitle, centerX - subtitleW / 2, startY);

        // Draw total number
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        g2.drawString(totalText,
                centerX - totalW / 2,
                startY + fmSmall.getHeight() + gap + fmBig.getAscent() - fmBig.getDescent());
    }

    public void drawColorNotation(Graphics2D g2, int size) {

        int offsetY = model.getDistributionBoxHeight() / 4;

        for (int i = 0; i < colors.length; i++) {
            colorNotation(g2, colors[i], size + 20, offsetY);
            offsetY += 40;
        }
    }

    public void drawQuestionTypes(Graphics2D g2, int size) {

        int offsetY = model.getDistributionBoxHeight() / 4 + 15;

        g2.setFont(new Font("Arial", Font.PLAIN, 14));

        for (int i = 0; i < labels.length; i++) {
            g2.setColor(Color.BLACK);
            g2.drawString(labels[i], size + 50, offsetY);
            g2.drawString(String.valueOf(values.get(i)), size + 150, offsetY);
            offsetY += 40;
        }
    }

    public void colorNotation(Graphics2D g2, Color color, int offsetX, int offsetY) {
        g2.setColor(color);
        g2.fillRoundRect(offsetX, offsetY, 20, 20, 5, 5);
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        calculateValues();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width  = 300;
        int height = 300;
        int size   = Math.min(width, height) - 100;
        int x      = (width - size) / 6;
        int y      = (height - size) / 2;

        if (total > 0) drawColoredGraph(g2, size, x, y);

        drawInnerCircle(g2, size, x, y);
        drawInnerText(g2, size, x, y);   // ← fixed signature passes size, x, y
        drawColorNotation(g2, size);
        drawQuestionTypes(g2, size);
    }
}
