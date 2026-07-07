package org.example;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.*;
import java.util.List;

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

        List<Question> questions = model.getQuestions();
        if (questions.isEmpty()) return;

        List<List<BufferedImage>> questionImages = extractImages(questions);

        for (int qi = 0; qi < questions.size(); qi++) {

            Question question = questions.get(qi);
            List<BufferedImage> images = questionImages.get(qi);

            JPanel cardPanel = new JPanel();
            cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
            cardPanel.setOpaque(false);
            cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));

            String html = "<html><body style='width:350px;font-family:Arial;font-size:14px;'>"
                    + question.format().replace("\n", "<br>")
                    + "</body></html>";

            JLabel textLabel = new JLabel(html);
            textLabel.setVerticalAlignment(JLabel.TOP);
            textLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            cardPanel.add(textLabel);

            for (BufferedImage img : images) {
                if (img == null) continue;
                cardPanel.add(Box.createVerticalStrut(8));
                JLabel imgLabel = new JLabel(scaleImage(img));
                imgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                cardPanel.add(imgLabel);
            }

            JScrollPane scroll = new JScrollPane(cardPanel);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            add(scroll);
        }
    }

    // ----------------------------------------------------------------
    // Extract images from Word file grouped by question.
    //
    // Strategy:
    //   1. First pass: find the paragraph index of each question stem.
    //   2. Second pass: for each image paragraph, find which question's
    //      stem range [stemStart, nextStemStart) it falls in.
    //      This ensures images only belong to their own question.
    // ----------------------------------------------------------------

    private List<List<BufferedImage>> extractImages(List<Question> questions) {

        List<List<BufferedImage>> result = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) result.add(new ArrayList<>());

        if (model.getSelectedFile() == null) return result;

        try (FileInputStream fis = new FileInputStream(model.getSelectedFile());
             XWPFDocument doc = new XWPFDocument(fis)) {

            List<XWPFParagraph> paras = doc.getParagraphs();

            // ── Step 1: Find stem paragraph index for each question ──
            // stemParaIndex[qi] = paragraph index where question qi's stem is
            int[] stemParaIndex = new int[questions.size()];
            Arrays.fill(stemParaIndex, -1);

            for (int qi = 0; qi < questions.size(); qi++) {
                String stem = cleanStem(questions.get(qi).format());
                if (stem.isBlank()) continue;
                String stemPrefix = stem.substring(0, Math.min(stem.length(), 20));

                for (int pi = 0; pi < paras.size(); pi++) {
                    String paraText = cleanStem(paras.get(pi).getText());
                    if (paraText.length() >= 5
                            && (paraText.startsWith(stemPrefix)
                            || stemPrefix.startsWith(paraText.substring(0,
                            Math.min(paraText.length(), 20))))) {
                        stemParaIndex[qi] = pi;
                        break;
                    }
                }
            }

            System.out.println("Stem paragraph indices: " + Arrays.toString(stemParaIndex));

            // ── Step 2: For each image paragraph, assign to correct question ──
            for (int pi = 0; pi < paras.size(); pi++) {

                XWPFParagraph para = paras.get(pi);
                String xml = para.getCTP().xmlText();
                if (!xml.contains("w:drawing") && !xml.contains("w:pict")) continue;

                // Find which question this image belongs to:
                // It belongs to question qi if:
                //   stemParaIndex[qi] <= pi < stemParaIndex[qi+1]
                int ownerQi = -1;
                for (int qi = 0; qi < questions.size(); qi++) {
                    int stemStart = stemParaIndex[qi];
                    if (stemStart < 0) continue;

                    // Next question's stem start (or end of document)
                    int nextStemStart = paras.size();
                    for (int nqi = qi + 1; nqi < questions.size(); nqi++) {
                        if (stemParaIndex[nqi] >= 0) {
                            nextStemStart = stemParaIndex[nqi];
                            break;
                        }
                    }

                    if (pi >= stemStart && pi < nextStemStart) {
                        ownerQi = qi;
                        break;
                    }
                }

                if (ownerQi < 0) {
                    System.out.println("Image at para " + pi + " — no matching question found, skipping.");
                    continue;
                }

                // Extract images from this paragraph
                for (XWPFRun run : para.getRuns()) {
                    for (XWPFPicture pic : run.getEmbeddedPictures()) {
                        try {
                            byte[] data = pic.getPictureData().getData();
                            BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                            if (img != null) {
                                System.out.println("Image at para " + pi + " → question " + ownerQi);
                                result.get(ownerQi).add(img);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("QuestionPreviewer image extraction failed: " + e.getMessage());
        }

        return result;
    }

    private String cleanStem(String text) {
        if (text == null) return "";
        return text.replaceFirst("^\\s*\\d+[.)\\-]\\s*", "").trim().toLowerCase();
    }

    private ImageIcon scaleImage(BufferedImage img) {
        int maxW = 350, maxH = 250;
        int w = img.getWidth(), h = img.getHeight();
        if (w > maxW || h > maxH) {
            double scale = Math.min((double) maxW / w, (double) maxH / h);
            w = (int) (w * scale);
            h = (int) (h * scale);
        }
        return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(217, 217, 217));
        g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 10, 10);
    }
}
