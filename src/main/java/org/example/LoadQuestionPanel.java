package org.example;

import org.example.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

public class LoadQuestionPanel extends JPanel implements DashBoardInnerPanel {

    private final JLabel loadQuestionLabel         = new JLabel("1. Load Question");
    private final JLabel loadQuestionFromWordLabel = new JLabel("Load question from word file");


    private final Image word =  new ImageIcon(
            getClass().getResource("/Image/word.png")).
            getImage().getScaledInstance(50, 30, Image.SCALE_SMOOTH
            );

    private File wordFile;
    private final MyButton browseButton = new MyButton();

    private static final int WORD_BOX_X      = 10;
    private static final int WORD_BOX_Y      = 100;
    private static final int WORD_BOX_WIDTH  = 280;
    private static final int WORD_BOX_HEIGHT = 100;

    private final Model model;

    public LoadQuestionPanel(Model model) {
        this.model = model;
        setPanelProperties();
        setLayout(null);
        setLabels();
        handleEvent();
    }

    public void createPanel(Graphics2D g2) {
        g2.setColor(new Color(217, 217, 217));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        wordFileBox(g2);
    }

    public void setPanelProperties() {
        setBounds(
                model.getLoadQuestionBoxX(),
                model.getLoadQuestionBoxY(),
                model.getLoadQuestionBoxWidth(),
                model.getLoadQuestionBoxHeight()
        );
        browseButton.setText("Browse");
        browseButton.setBounds(getWidth() - 110, getHeight() - 50, 100, 40);
    }

    public void setLabels() {
        setTitleLabelFont(loadQuestionLabel);
        loadQuestionLabel.setBounds(10, 5, model.getLoadQuestionBoxWidth(), 50);

        setTextLabelFont(loadQuestionFromWordLabel);
        loadQuestionFromWordLabel.setBounds(10, 50, model.getLoadQuestionBoxWidth(), 50);

        add(loadQuestionLabel);
        add(loadQuestionFromWordLabel);
        add(browseButton);
    }

    public void handleEvent() {

        JLabel fileInfoLabel = new JLabel();
        JLabel fileSizeLabel = new JLabel();

        fileInfoLabel.setBounds(WORD_BOX_X,      WORD_BOX_Y, WORD_BOX_WIDTH, WORD_BOX_HEIGHT);
        fileSizeLabel.setBounds(WORD_BOX_X + 50, WORD_BOX_Y, WORD_BOX_WIDTH, WORD_BOX_HEIGHT);

        add(fileInfoLabel);
        add(fileSizeLabel);

        browseButton.addActionListener((ActionEvent e) -> {

            // 1. File chooser
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) return;

            wordFile = chooser.getSelectedFile();
            model.setSelectedFile(wordFile);

            // 2. Show loading message
            fileInfoLabel.setText("Analyzing with AI...");
            fileInfoLabel.setIcon(null);
            revalidate();
            repaint();

            // 3. Extract questions in background so UI doesn't freeze
            SwingWorker<List<Question>, Void> worker = new SwingWorker<>() {

                @Override
                protected List<Question> doInBackground() {
                    // QuestionExtractor uses Groq online, tags offline
                    return QuestionExtractor.extract(wordFile, model);
                }

                @Override
                protected void done() {
                    try {
                        List<Question> questions = get();

                        System.out.println("Total questions loaded: " + questions.size());
                        model.setQuestions(questions);

                        // Update file info UI
                        DecimalFormat df = new DecimalFormat("#.##");
                        double fileSize = wordFile.length() / 1024.0;

                        fileInfoLabel.setIcon(new ImageIcon(word));
                        fileInfoLabel.setText(wordFile.getName());
                        setTextLabelFont(fileInfoLabel);
                        fileInfoLabel.setBorder(
                                BorderFactory.createEmptyBorder(10, 5, 5, 10));
                        fileInfoLabel.setVerticalTextPosition(SwingConstants.TOP);
                        fileInfoLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
                        fileInfoLabel.setHorizontalAlignment(JLabel.LEFT);
                        fileInfoLabel.setVerticalAlignment(JLabel.TOP);
                        fileInfoLabel.setIconTextGap(10);

                        fileSizeLabel.setText(df.format(fileSize) + " KB");
                        setTextLabelFont(fileSizeLabel);

                        model.setDrawGraph(true);
                        revalidate();
                        repaint();

                        SwingUtilities.invokeLater(() -> {
                            Window w = SwingUtilities.getWindowAncestor(
                                    LoadQuestionPanel.this);
                            if (w != null) w.repaint();
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(LoadQuestionPanel.this,
                                "Error loading file: " + ex.getMessage());
                    }
                }
            };

            worker.execute();
        });
    }

    public void wordFileBox(Graphics2D g2) {
        g2.setColor(new Color(230, 230, 230));
        model.setWordFileBoxX(model.getLoadQuestionBoxX() + 10);
        model.setWordFileBoxY(model.getLoadQuestionBoxY() + 100);
        g2.fillRoundRect(WORD_BOX_X, WORD_BOX_Y, WORD_BOX_WIDTH, WORD_BOX_HEIGHT, 10, 10);
    }

    public void setTitleLabelFont(JLabel title) {
        title.setFont(new Font("Arial", Font.BOLD, 20));
    }

    public void setTextLabelFont(JLabel text) {
        text.setFont(new Font("Arial", Font.PLAIN, 16));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        createPanel(g2);
    }
}
