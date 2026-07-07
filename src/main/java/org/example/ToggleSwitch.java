package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

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

    public static void main(String[] args) {

        JFrame frame = new JFrame("Toggle Switch Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new FlowLayout());

        ToggleSwitch toggle = new ToggleSwitch();

        JLabel status = new JLabel("OFF");

        toggle.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                status.setText("ON");
            } else {
                status.setText("OFF");
            }
        });

        frame.add(toggle);
        frame.add(status);

        frame.setVisible(true);
    }
}