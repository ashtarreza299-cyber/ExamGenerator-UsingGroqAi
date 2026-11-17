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
import java.awt.*;

public class ExamSettingPanel extends JPanel implements DashBoardInnerPanel {

    private JSpinner numOfStd = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private JSpinner numOfQst = new JSpinner(new SpinnerNumberModel(0, 0, 300, 1));


    private final JSpinner[] spinners = {

            numOfStd,
            numOfQst
    };

    private ToggleSwitch shuffleQuestionButton = new ToggleSwitch();
    private ToggleSwitch shuffleOptionButton = new ToggleSwitch();
    private ToggleSwitch includeAnswerKeyButton = new ToggleSwitch();


    private ToggleSwitch shuffleMCQ = new ToggleSwitch();
    private ToggleSwitch shuffleDes = new ToggleSwitch();
    private ToggleSwitch shuffleBlank = new ToggleSwitch();
    private ToggleSwitch shuffleTrueFalse = new ToggleSwitch();

    private final ToggleSwitch[] ts = {

            includeAnswerKeyButton,
            shuffleOptionButton,
            shuffleQuestionButton,
            shuffleMCQ,
            shuffleDes,
            shuffleBlank,
            shuffleTrueFalse
    };

    private final JLabel examSettingLabel = new JLabel("2. Exam Setting");

    private final JLabel[] examSettingLabels = {

            new JLabel("Number of students / Exam"),
            new JLabel("Number of questions per exam"),

            new JLabel("Include answer key"),
            new JLabel("Shuffle options in MCQS"),
            new JLabel("Shuffle question"),

            new JLabel("Shuffle MCQ"),
            new JLabel("Shuffle Descriptive"),
            new JLabel("Shuffle Fill In Blank"),
            new JLabel("Shuffle True False")
    };

    Model model;

    public ExamSettingPanel(Model model) {

        this.model = model;

        setBackground(new Color(217, 217, 217));
        setUpPanel();
        setLayout(null);
        addComponent();
        handleEvent();
    }

    public void addComponent() {

        add(examSettingLabel);

        for(JLabel label: examSettingLabels)
            add(label);

        for(JSpinner spinner: spinners)
            add(spinner);

        for(ToggleSwitch t: ts)
            add(t);
    }

    public void setUpPanel() {

        setExamSettingBoxLabels();
        setNumberSpinnerAndToggleSwitch();

        setBounds(
                model.getExamSettingBoxX(),
                model.getExamSettingBoxY(),
                model.getExamSettingBoxWidth(),
                model.getExamSettingBoxHeight()
        );
    }

    public void createPanel(Graphics2D g2){

        g2.setColor(new Color(217, 217, 217));//230, 230, 230

        g2.fillRoundRect(0 , 0,
                model.getExamSettingBoxWidth(), model.getExamSettingBoxHeight(), 10, 10);

    }

    public void handleEvent() {

        numOfStd.addChangeListener(e -> {
            model.setNumberOfStudents((Integer) numOfStd.getValue());
        });

        numOfQst.addChangeListener(e -> {
            model.setNumberOfQuestion((Integer) numOfQst.getValue());
        });

        shuffleQuestionButton.addActionListener(e -> {

            model.setShufflingOn(shuffleQuestionButton.isSelected());

            if(!shuffleQuestionButton.isSelected()){
                shuffleMCQ.setSelected(false);
                shuffleDes.setSelected(false);
                shuffleBlank.setSelected(false);
                shuffleTrueFalse.setSelected(false);
            }
        });

        shuffleOptionButton.addActionListener(e -> {
            model.setOptionShufflingOn(shuffleOptionButton.isSelected());
        });

        includeAnswerKeyButton.addActionListener(e -> {

            model.setAnswerKeyIncluded(includeAnswerKeyButton.isSelected());
        });

        shuffleMCQ.addActionListener(e -> {

            if(!shuffleQuestionButton.isSelected()){
                shuffleMCQ.setSelected(false);
                JOptionPane.showMessageDialog(null, "To use this feature enable Shuffle Question",
                        "Enable Question Shuffle Option", JOptionPane.INFORMATION_MESSAGE);
            }else
                model.setMcqShuffled(shuffleMCQ.isSelected());

        });

        shuffleDes.addActionListener(e -> {
            if(!shuffleQuestionButton.isSelected()){
                shuffleDes.setSelected(false);
                JOptionPane.showMessageDialog(null, "To use this feature enable Shuffle Question",
                        "Enable Question Shuffle Option", JOptionPane.INFORMATION_MESSAGE);
            }else
                model.setDesShuffled(shuffleDes.isSelected());
        });

        shuffleTrueFalse.addActionListener(e -> {

            if(!shuffleQuestionButton.isSelected()){
                shuffleTrueFalse.setSelected(false);
                JOptionPane.showMessageDialog(null, "To use this feature enable Shuffle Question",
                        "Enable Question Shuffle Option", JOptionPane.INFORMATION_MESSAGE);
            }else
                model.setTrueFalseShuffled(shuffleTrueFalse.isSelected());
        });

        shuffleBlank.addActionListener(e -> {

            if(!shuffleQuestionButton.isSelected()){
                shuffleBlank.setSelected(false);
                JOptionPane.showMessageDialog(null, "To use this feature enable Shuffle Question",
                        "Enable Question Shuffle Option", JOptionPane.INFORMATION_MESSAGE);
            }else
                model.setBlankSpaceShuffled(shuffleBlank.isSelected());
        });
    }

    public void setExamSettingBoxLabels() {

        setTitleLabelFont(examSettingLabel);
        examSettingLabel.setBounds(20, 10, 250, 40);

        int offsetY = 50;
        for(JLabel label: examSettingLabels){

            setTextLabelFont(label);
            label.setBounds(20, offsetY,  model.getExamSettingBoxWidth() - 40, 30);
            offsetY += 40;
        }

    }

    public void setNumberSpinnerAndToggleSwitch() {

        int offsetY = 50;
        for(JSpinner spinner : spinners) {

            spinner.setBounds(
                    model.getExamSettingBoxWidth() - 100,
                    offsetY, 80, 30);


            offsetY += 40;
        }

        for(ToggleSwitch t: ts) {
            t.setBounds(
                    model.getExamSettingBoxWidth() - 100,
                    offsetY, 50, 30);
            offsetY += 40;
        }

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
