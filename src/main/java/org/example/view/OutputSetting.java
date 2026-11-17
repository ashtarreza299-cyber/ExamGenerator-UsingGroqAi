
package org.example.view;

import org.example.model.BlankSpaceQuestion;
import org.example.model.DescriptiveQuestion;
import org.example.model.ExamStructure;
import org.example.model.Model;
import org.example.model.ModelListener;
import org.example.model.MultipleChoiceQuestion;
import org.example.model.Question;
import org.example.model.TrueFalseQuestion;
import org.example.view.DashBoardInnerPanel;
import org.example.view.DashBoardPanel;
import org.example.view.ExamSettingPanel;
import org.example.view.LoadQuestionPanel;
import org.example.view.OutputSetting;
import org.example.view.ProgramViewPanel;
import org.example.view.QuestionDistributionPanel;
import org.example.view.QuestionPreviewPanel;
import org.example.view.QuestionPreviewer;
import org.example.view.QuestionTypePanel;
import org.example.view.SideBarPanel;
import org.example.view.component.ExamComponent;
import org.example.view.component.MyButton;
import org.example.view.component.MyCheckBox;
import org.example.view.component.NumberSpinner;
import org.example.view.component.QuestionGraph;
import org.example.view.component.RoundedField;
import org.example.view.component.RoundedFileChooserField;
import org.example.view.component.RoundedPanel;
import org.example.view.component.ToggleSwitch;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class OutputSetting extends JPanel implements DashBoardInnerPanel {

    private final JLabel title = new JLabel("4.Output Setting");
    private final JLabel outputFolderLabel = new JLabel("Output Folder");
    private final JLabel fileNamingLabel = new JLabel("File Naming");
    private final JLabel startNumberLabel = new JLabel("Start Number From");
    private final RoundedFileChooserField fileChooser;
    private final RoundedField fileName = new RoundedField();
    private final NumberSpinner spinner = new NumberSpinner();

    Model model;
    public OutputSetting(Model model) {

        this.model = model;

        fileChooser = new RoundedFileChooserField(model);

        setPanelProperties();
        setLayout(null);
        addComponents();
        setLabels();
        handleEvent();

    }

    public void createPanel(Graphics2D g2) {

        g2.setColor(new Color(217, 217, 217));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
    }

    public void setPanelProperties(){
        setBounds(
                model.getQuestionTypePanelWidth() + 30,
                model.getExamSettingBoxHeight() + model.getExamSettingBoxY() + 10,
                model.getDashBoardWidth() - model.getQuestionTypePanelWidth() - 50,
                model.getQuestionTypePanelHeight());

    }

    public void handleEvent() {

        fileChooser.getTextField().addActionListener(

                e -> model.setOutputFolder(fileChooser.getChooser().getSelectedFile())
        );

        // When the browse button is used inside the RoundedFileChooserField, the internal
        // handler will set the text field. We add a listener that updates the model after
        // the internal chooser action completes.

        fileChooser.getBrowseButton().addActionListener(e -> SwingUtilities.invokeLater(() -> {
            model.setOutputFolder(fileChooser.getChooser().getSelectedFile());
        }));

        fileName.getTextField().getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        model.setFileNaming(fileName.getText());
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        model.setFileNaming(fileName.getText());
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        model.setFileNaming(fileName.getText());
                    }
                }
        );
        spinner.getSpinner().addChangeListener(e -> {
            model.setStartNumber((Integer) spinner.getSpinner().getValue());
        });

    }

    public void addComponents() {
        add(title);
        add(outputFolderLabel);
        add(fileNamingLabel);
        add(startNumberLabel);
        add(fileChooser);
        add(fileName);
        add(spinner);
    }

    public void setLabels() {

        setTitleLabelFont(title);
        title.setBounds(10, 10, getWidth(), 50);

        setTextLabelFont(outputFolderLabel);
        outputFolderLabel.setBounds(10, 50, getWidth(), 50);

        fileChooser.setBounds(10, 90,  getWidth() - 20, 50);

        setTextLabelFont(fileNamingLabel);
        fileNamingLabel.setBounds(10, 140, getWidth() - 20, 40);

        fileName.setBounds(10, 180, getWidth() - 20, 40);

        setTextLabelFont(startNumberLabel);
        startNumberLabel.setBounds(10, 210, getWidth(), 50);

        spinner.setBounds(10, 250, getWidth() - 20, 40);
    }

    public void setTitleLabelFont(JLabel title){
        title.setFont(new Font("Arial", Font.BOLD, 20));
    }

    public void setTextLabelFont(JLabel text){
        text.setFont(new Font("Arial", Font.PLAIN, 16));
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 =  (Graphics2D) g;

        createPanel(g2);
    }

}
