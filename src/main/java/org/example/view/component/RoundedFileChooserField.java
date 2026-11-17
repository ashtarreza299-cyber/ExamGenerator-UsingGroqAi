package org.example.view.component;

import org.example.model.BlankSpaceQuestion;
import org.example.model.DescriptiveQuestion;
import org.example.model.ExamStructure;
import org.example.model.Model;
import org.example.model.ModelListener;
import org.example.model.MultipleChoiceQuestion;
import org.example.model.Question;
import org.example.model.TrueFalseQuestion;

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

        browseButton = new JButton("Browse");

        browseButton.setFocusPainted(false);
        browseButton.setBackground(new Color(0, 120, 255));
        browseButton.setForeground(Color.WHITE);
        browseButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        browseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        browseButton.setFocusPainted(false);
        browseButton.setContentAreaFilled(false);
        browseButton.setOpaque(true);

        browseButton.addActionListener(e -> openFileChooser());

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