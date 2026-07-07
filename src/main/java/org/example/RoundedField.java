package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RoundedField extends JPanel{

    private JTextField textField;

    public RoundedField() {

        textField = new JTextField();

        setLayout(new BorderLayout());

        textField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        textField.setForeground(Color.BLACK);
        textField.setBackground(Color.WHITE);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setOpaque(false);

        setBackground(Color.WHITE);
        setOpaque(false);

        JPanel container = new RoundedPanel(20, new BorderLayout());
        container.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        container.add(textField, BorderLayout.CENTER);

        add(container, BorderLayout.CENTER);
    }

    // ---------------------------
    // TEXT CONTROL METHODS
    // ---------------------------
    public String getText() {
        return textField.getText();
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public JTextField getTextField() {
        return textField;
    }

    // ---------------------------
    // EVENT LISTENERS
    // ---------------------------

    public void addActionListener(ActionListener listener) {
        textField.addActionListener(listener);
    }

    public void removeActionListener(ActionListener listener) {
        textField.removeActionListener(listener);
    }

    public void addKeyListener(KeyListener listener) {
        textField.addKeyListener(listener);
    }

    public void removeKeyListener(KeyListener listener) {
        textField.removeKeyListener(listener);
    }

    public void addFocusListener(FocusListener listener) {
        textField.addFocusListener(listener);
    }

    public void removeFocusListener(FocusListener listener) {
        textField.removeFocusListener(listener);
    }
}