package org.example;

import javax.swing.*;
import java.awt.*;

public class QuestionPreviewPanel extends JPanel {

    private JLabel title = new JLabel("Question Preview");

    private MyButton next = new MyButton();
    private MyButton previous = new MyButton();

    private JLabel pageLabel = new JLabel();

    private QuestionPreviewer previewer;

    private int currentIndex = 0;

    private Model model;

    public QuestionPreviewPanel(Model model) {

        this.model = model;

        previewer = new QuestionPreviewer(model);

        setPanelProperties();
        setTitle();
        setButtons();
        setPageLabel();
        previewer.setBounds(10, 60, getWidth() - 20, getHeight() - 70);
        addComponents();
        updatePageLabel();
        handleEvent();
        model.addModelListener(() -> SwingUtilities.invokeLater(this::refreshPreview));
    }

    private void setPanelProperties() {

        setBounds(
                model.getDistributionBoxX(),
                model.getDistributionBoxHeight() + 10,
                model.getDistributionBoxWidth(),
                model.getPanelHeight() - model.getDistributionBoxHeight() - 30
        );

        setBackground(new Color(230, 230, 230));
        setLayout(null);
    }

    private void setTitle() {
        // Title
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBounds(10, 10, 200, 40);
    }

    private void setButtons() {

        // Buttons
        next.setText(">");
        previous.setText("<");

        next.setBounds(280, 10, 50, 40);
        previous.setBounds(190, 10, 50, 40);

    }

    private void setPageLabel() {
        // Page label
        pageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        pageLabel.setBounds(200, 10, 120, 40);
        pageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void addComponents() {
        add(title);
        add(previous);
        add(pageLabel);
        add(next);
        add(previewer);
    }

    private void handleEvent() {
        // NEXT
        next.addActionListener(e -> {

            int total = model.getQuestions().size();

            if (total == 0) return;

            if (currentIndex < total - 1) {
                currentIndex++;
                previewer.getCardLayout().next(previewer);
                updatePageLabel();
            }
        });


        previous.addActionListener(e -> {

            if (currentIndex > 0) {
                currentIndex--;
                previewer.getCardLayout().previous(previewer);
                updatePageLabel();
            }
        });
    }

    public void refreshPreview() {

        previewer.removeAll();
        previewer.setQuestion();

        currentIndex = 0;

        previewer.getCardLayout().first(previewer);

        updatePageLabel();

        revalidate();
        repaint();
    }

    private void updatePageLabel() {

        int total = model.getQuestions().size();

        if (total == 0) {
            pageLabel.setText("0 / 0");
        } else {
            pageLabel.setText((currentIndex + 1) + " / " + total);
        }
    }
}