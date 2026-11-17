package org.example.view.component;


import javax.swing.*;
import java.awt.*;
public class ToggleSwitch extends JToggleButton implements ExamComponent {

    private Color onColor = new Color(0, 122, 255);   // iOS blue
    private Color offColor = new Color(200, 200, 200);

    public ToggleSwitch() {

        setPreferredSize(new Dimension(60, 30));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);

        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addItemListener(e -> repaint());
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background (track)
        if (isSelected()) {
            g2.setColor(onColor);
        } else {
            g2.setColor(offColor);
        }

        g2.fillRoundRect(0, 0, width, height, height, height);

        // Knob
        int size = height - 6;
        int x = isSelected() ? (width - size - 3) : 3;
        int y = 3;

        g2.setColor(Color.WHITE);
        g2.fillOval(x, y, size, size);

        g2.dispose();
    }


}