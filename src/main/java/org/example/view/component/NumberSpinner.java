package org.example.view.component;


import javax.swing.*;
import java.awt.*;

public class NumberSpinner extends JPanel {

    JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));

    public NumberSpinner() {

        setLayout(new BorderLayout());

        spinner.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        spinner.setForeground(Color.BLACK);
        spinner.setBackground(Color.WHITE);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinner.setOpaque(false);

        setBackground(Color.WHITE);
        setOpaque(false);

        JPanel container = new RoundedPanel(20, new BorderLayout());
        container.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        container.add(spinner, BorderLayout.CENTER);

        add(container, BorderLayout.CENTER);
    }

    public JSpinner getSpinner() {
        return spinner;
    }


}
