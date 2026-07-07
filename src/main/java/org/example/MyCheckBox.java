package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MyCheckBox extends JComponent implements ExamComponent {

    private boolean selected;

    private final Color ON_COLOR = new Color(0, 122, 255);
    private final Color OFF_COLOR = Color.WHITE;
    private final Color BORDER_COLOR = new Color(220, 220, 220);

    public MyCheckBox() {

        setPreferredSize(new Dimension(32, 32));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(!selected);
            }
        });
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();

        firePropertyChange("selected", !selected, selected);
    }

    @Override
    public void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int arc = 12;

        if (selected) {
            g2.setColor(ON_COLOR);
            g2.fillRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    arc,
                    arc
            );

            g2.setStroke(new BasicStroke(
                    3f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            g2.setColor(Color.WHITE);

            int w = getWidth();
            int h = getHeight();

            g2.drawLine(
                    w / 4,
                    h / 2,
                    w / 2 - 2,
                    h * 3 / 4
            );

            g2.drawLine(
                    w / 2 - 2,
                    h * 3 / 4,
                    w * 3 / 4,
                    h / 4
            );

        } else {

            g2.setColor(OFF_COLOR);
            g2.fillRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    arc,
                    arc
            );

            g2.setColor(BORDER_COLOR);
            g2.setStroke(new BasicStroke(2f));

            g2.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    arc,
                    arc
            );
        }

        g2.dispose();
    }
}