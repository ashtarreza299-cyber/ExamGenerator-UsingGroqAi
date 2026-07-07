package org.example;

import javax.swing.*;
import java.awt.*;

public class QuestionPreviewer extends JPanel {

    private CardLayout card = new CardLayout();

    Model model;
    public QuestionPreviewer(Model model) {

        this.model = model;
        setLayout(card);
        setOpaque(false);

    }

    public CardLayout getCardLayout() {
        return card;
    }

    public void setQuestion() {
        // Rebuild preview cards from model questions
        removeAll();
        int idx = 0;
        for (Question question : model.getQuestions()) {
            String questionText = question.format().replaceAll("\\r?\\n", "<br>");
            StringBuilder builder = new StringBuilder();
            builder.append("<html>" + questionText + "</html>");

            JLabel q = new JLabel(builder.toString());
            q.setFont(new Font("Arial", Font.PLAIN, 20));
            q.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 5));
            q.setVerticalAlignment(JLabel.TOP);
            q.setHorizontalAlignment(JLabel.LEFT);
            add(q, "q" + idx);
            idx++;
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(new Color(217, 217, 217));
        g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 10, 10);
        g2.setColor(Color.BLACK);
    }


}
