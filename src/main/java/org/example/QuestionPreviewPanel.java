package org.example;

import javax.swing.*;
import java.awt.*;

public class QuestionPreviewPanel extends JPanel implements ModelListener {

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

        setBounds(
                model.getDistributionBoxX(),
                model.getDistributionBoxHeight() + 10,
                model.getDistributionBoxWidth(),
                model.getPanelHeight() - model.getDistributionBoxHeight() - 30
        );

        setBackground(new Color(230, 230, 230));
        setLayout(null);

        // Title
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBounds(10, 10, 200, 40);

        // Buttons
        next.setText(">");
        previous.setText("<");

        next.setBounds(280, 10, 50, 40);
        previous.setBounds(190, 10, 50, 40);

        // Page label
        pageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        pageLabel.setBounds(200, 10, 120, 40);
        pageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Previewer
        previewer.setBounds(
                10,
                60,
                getWidth() - 20,
                getHeight() - 70
        );

        add(title);
        add(previous);
        add(pageLabel);
        add(next);
        add(previewer);

        // initial label
        updatePageLabel();

        // register for model changes so pageLabel and preview update when questions load
        model.addModelListener(this);
        // initial preview
        refreshPreview();

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
        // Recompute layout of preview controls based on current size
        // Buttons on the right
        int btnWidth = 50;
        int spacing = 10;
        next.setBounds(getWidth() - btnWidth - spacing, 10, btnWidth, 40);
        previous.setBounds(getWidth() - (2 * btnWidth) - (2 * spacing), 10, btnWidth, 40);
        pageLabel.setBounds(getWidth() - (3 * btnWidth) - (3 * spacing), 10, 120, 40);

        // Previewer occupies remaining area
        previewer.setBounds(10, 60, Math.max(100, getWidth() - 20), Math.max(100, getHeight() - 70));

        previewer.removeAll();
        previewer.setQuestion();

        currentIndex = 0;

        if (previewer.getComponentCount() > 0) {
            previewer.getCardLayout().first(previewer);
        }

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

    @Override
    public void modelChanged() {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            refreshPreview();
        } else {
            javax.swing.SwingUtilities.invokeLater(this::refreshPreview);
        }
    }
}