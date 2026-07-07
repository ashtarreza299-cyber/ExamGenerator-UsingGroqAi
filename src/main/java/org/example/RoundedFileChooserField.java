package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class RoundedFileChooserField extends JPanel {

    private final JTextField textField;
    private final JButton browseButton;
    private final JFileChooser chooser = new JFileChooser();
    Model model;

    public RoundedFileChooserField(Model model) {

        this.model = model;

        setLayout(new BorderLayout());
        setOpaque(false);

        // TEXT FIELD
        textField = new JTextField("No file selected");
        textField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        textField.setEditable(false);
        textField.setBackground(Color.WHITE);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setOpaque(false);

        // BROWSE BUTTON
        browseButton = new JButton("Browse");
        browseButton.setFocusPainted(false);
        browseButton.setForeground(Color.WHITE);
        browseButton.setBackground(new Color(0, 120, 255));
        browseButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        browseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // SINGLE CORRECT ACTION LISTENER
        browseButton.addActionListener(e -> openFileChooser());

        // CONTAINER
        JPanel container = new RoundedPanel(18, new BorderLayout());
        container.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        container.setBackground(Color.WHITE);
        container.setOpaque(false);

        container.add(textField, BorderLayout.CENTER);
        container.add(browseButton, BorderLayout.EAST);

        add(container, BorderLayout.CENTER);
    }

    private void openFileChooser() {

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(null);

        if(result == JFileChooser.CANCEL_OPTION) return;

        File file = chooser.getSelectedFile();
        // USER CANCELLED
        if (file == null || file.getAbsolutePath().equals("")) {
            return;
        }
        setSelectedFilePath(file.getAbsolutePath());
        model.setOutputFolder(file);
    }

    public void setSelectedFilePath(String path) {
        textField.setText(path);
    }

    public String getSelectedFilePath() {
        return textField.getText();
    }

    public JButton getBrowseButton() {
        return browseButton;
    }

    public JTextField getTextField() {
        return textField;
    }

    public JFileChooser getChooser() {
        return  chooser;
    }

    public File getSelectedFile() {
        return chooser.getSelectedFile();
    }

}