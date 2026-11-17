package org.example.view.component;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MyButton extends JButton implements ExamComponent {

    private boolean isHovered = false;
    private boolean isPressed = false;

    private Timer hoverTimer;
    private float animationProgress = 0f;

    public MyButton() {

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);

        setForeground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        initMouseListeners();
        initHoverTimer();
    }

    private void initMouseListeners() {

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                hoverTimer.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                hoverTimer.start();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    private void initHoverTimer() {

        hoverTimer = new Timer(15, e -> {

            if (isHovered && animationProgress < 1f) {
                animationProgress += 0.05f;
            }
            else if (!isHovered && animationProgress > 0f) {
                animationProgress -= 0.05f;
            }

            animationProgress = Math.max(0f,
                    Math.min(1f, animationProgress));

            if ((!isHovered && animationProgress <= 0f)
                    || (isHovered && animationProgress >= 1f)) {
                hoverTimer.stop();
            }

            repaint();
        });
    }

    @Override
    public Dimension getPreferredSize() {

        Font font = new Font("Arial", Font.BOLD, 16);
        FontMetrics fm = getFontMetrics(font);

        String text = getText();
        Icon icon = getIcon();

        int textWidth = text == null ? 0 : fm.stringWidth(text);
        int textHeight = text == null ? 0 : fm.getHeight();

        int iconWidth = icon == null ? 0 : icon.getIconWidth();
        int iconHeight = icon == null ? 0 : icon.getIconHeight();

        int gap = (icon != null && text != null) ? 10 : 0;

        int width = iconWidth + gap + textWidth + 30;
        int height = Math.max(iconHeight, textHeight) + 20;

        return new Dimension(width, height);
    }

    @Override
    public void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color normalColor = new Color(25, 25, 112);
        Color hoverColor = new Color(0, 140, 255);

        float t = animationProgress;

        int r = (int) (normalColor.getRed()
                + t * (hoverColor.getRed() - normalColor.getRed()));

        int gr = (int) (normalColor.getGreen()
                + t * (hoverColor.getGreen() - normalColor.getGreen()));

        int b = (int) (normalColor.getBlue()
                + t * (hoverColor.getBlue() - normalColor.getBlue()));

        Color background = new Color(r, gr, b);

        if (isPressed) {
            background = background.darker();
        }

        g2.setColor(background);

        g2.fillRoundRect(
                0,
                0,
                width,
                height,
                20,
                20
        );

        Font font = new Font("Arial", Font.BOLD, 16);
        g2.setFont(font);

        FontMetrics fm = g2.getFontMetrics();

        String text = getText();
        Icon icon = getIcon();

        int paddingLeft = 15;

        if (icon != null) {

            int iconY = (height - icon.getIconHeight()) / 2;

            icon.paintIcon(
                    this,
                    g2,
                    paddingLeft,
                    iconY
            );
        }

        if (text != null) {

            int iconWidth = icon == null ? 0 : icon.getIconWidth();

            int textX = paddingLeft + iconWidth + 12;

            int textY =
                    (height - fm.getHeight()) / 2
                            + fm.getAscent();

            g2.setColor(getForeground());

            g2.drawString(
                    text,
                    textX,
                    textY
            );
        }

        g2.dispose();
    }

}