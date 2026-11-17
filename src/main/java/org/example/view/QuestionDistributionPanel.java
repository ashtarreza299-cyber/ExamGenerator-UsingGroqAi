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

public class QuestionDistributionPanel extends JPanel{

    private JLabel title = new JLabel("Question Type Distribution");

    private Model model;

    public  QuestionDistributionPanel(Model model) {

        this.model = model;
        setDistributionBox();
        setProperties();
        add(new QuestionGraph(model));
        add(title);

    }

    public void createPanel(Graphics2D g2) {
        g2.setColor(new Color(230, 230, 230));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
    }

    private void setDistributionBox() {
        model.setDistributionBoxX(model.getSideBarWidth() + model.getDashBoardWidth() + 20);
        model.setDistributionBoxWidth(model.getPanelWidth() - model.getDistributionBoxX() - 10);
        model.setDistributionBoxHeight(300);
        setBounds(
                model.getDistributionBoxX(),
                0,
                model.getDistributionBoxWidth(),
                model.getDistributionBoxHeight()
        );
    }

    private void setProperties() {
        setBackground(Color.white);
        setLayout(null);
        setTitleLabelFont(title);
        title.setBounds(10, 10, getWidth(), 50);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D  g2 = (Graphics2D) g;

        createPanel(g2);

    }

    public void setTitleLabelFont(JLabel title){
        title.setFont(new Font("Arial", Font.BOLD, 20));
    }


}
