package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class ApiKeyManager {

    private static final String CONFIG_DIR  = System.getProperty("user.home") + "/.examgenerator";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.properties";
    private static final String KEY_NAME    = "GROQ_API_KEY";

    public static String initialize() {

        String saved = loadKey();

        if (saved != null) {
            return saved; // Already set up — silent load
        }

        // First launch — show setup dialog
        return showSetupDialog();
    }

    public static String loadKey() {

        File f = new File(CONFIG_FILE);
        if (!f.exists()) return null;

        try (FileInputStream fis = new FileInputStream(f)) {
            Properties props = new Properties();
            props.load(fis);
            String key = props.getProperty(KEY_NAME, "").trim();
            return key.isBlank() ? null : key;
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveKey(String key) {

        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));
            Properties props = new Properties();
            props.setProperty(KEY_NAME, key.trim());
            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                props.store(fos, "Exam Generator Configuration");
            }
        } catch (Exception e) {
            System.err.println("Could not save API key: " + e.getMessage());
        }
    }

    private static String showSetupDialog() {

        // Outer panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Instructions
        JLabel info = new JLabel("<html>" +
                "<b>Welcome to Exam Generator!</b><br><br>" +
                "This app uses Groq AI to detect question types automatically.<br>" +
                "You need a <b>free</b> Groq API key to use this feature.<br><br>" +
                "To get your free key:<br>" +
                "1. Go to <b>console.groq.com</b><br>" +
                "2. Sign up with your email (free, no credit card)<br>" +
                "3. Click <b>API Keys</b> → Create new key<br>" +
                "4. Paste it below<br><br>" +
                "</html>");
        panel.add(info);

        // Key input field
        JPasswordField keyField = new JPasswordField(40);
        keyField.setFont(new Font("Monospaced", Font.PLAIN, 13));
        keyField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        panel.add(keyField);
        panel.add(Box.createVerticalStrut(8));

        // Status label
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(statusLabel);

        // Buttons
        JButton verifyButton = new JButton("Verify & Save");
        JButton skipButton   = new JButton("Skip (use tags instead)");

        verifyButton.setBackground(new Color(25, 25, 112));
        verifyButton.setForeground(Color.WHITE);
        verifyButton.setFocusPainted(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(skipButton);
        buttonPanel.add(verifyButton);
        panel.add(buttonPanel);

        // Create dialog
        JDialog dialog = new JDialog((Frame) null, "Setup — Groq API Key", true);
        dialog.setContentPane(panel);
        dialog.setSize(480, 340);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        final String[] result = {null};

        verifyButton.addActionListener(e -> {

            String key = new String(keyField.getPassword()).trim();

            if (key.isBlank()) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Please paste your API key first.");
                return;
            }

            statusLabel.setForeground(Color.GRAY);
            statusLabel.setText("Verifying...");
            verifyButton.setEnabled(false);

            // Verify in background so UI doesn't freeze
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return GroqClassifier.verifyKey(key);
                }

                @Override
                protected void done() {
                    try {
                        boolean valid = get();
                        if (valid) {
                            saveKey(key);
                            result[0] = key;
                            statusLabel.setForeground(new Color(0, 150, 0));
                            statusLabel.setText("✓ Key verified and saved! You're all set.");
                            Timer t = new Timer(1200, ev -> dialog.dispose());
                            t.setRepeats(false);
                            t.start();
                        } else {
                            statusLabel.setForeground(Color.RED);
                            statusLabel.setText("✗ Invalid key. Please check and try again.");
                            verifyButton.setEnabled(true);
                        }
                    } catch (Exception ex) {
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText("Error verifying key. Check your internet.");
                        verifyButton.setEnabled(true);
                    }
                }
            };
            worker.execute();
        });

        skipButton.addActionListener(e -> {
            result[0] = null;
            dialog.dispose();
        });

        dialog.setVisible(true); // blocks until disposed
        return result[0];
    }
}
