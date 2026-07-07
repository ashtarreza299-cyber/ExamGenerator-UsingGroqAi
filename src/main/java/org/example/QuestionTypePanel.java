package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class QuestionTypePanel extends JPanel implements DashBoardInnerPanel{

    private JLabel title =  new JLabel("3.Question Type");
    private JLabel subtitle =  new JLabel("Select which question type to include");

    private ImageIcon[] icons = new ImageIcon[4];
    private ImageIcon[] scaled = new ImageIcon[4];

    private JLabel[] questionTypeLabels = {

            new JLabel("Multiple Choice"),
            new JLabel("True / False"),
            new JLabel("Fill In The Blank"),
            new JLabel("Descriptive"),
    };

    private MyCheckBox mcq = new MyCheckBox();
    private MyCheckBox trueFalse = new MyCheckBox();
    private MyCheckBox blank = new MyCheckBox();
    private MyCheckBox des = new MyCheckBox();

    private MyCheckBox[] checkBoxes = {

            mcq,
            trueFalse,
            blank,
            des
    };


    Model model;
    public QuestionTypePanel(Model model) {

        this.model = model;
        loadImages();
        setPanelProperties();
        setLayout(null);
        addComponentToPanel();
        setLabels();
        setCheckBoxes();
        handleEvent();
    }

    public void createPanel(Graphics2D g2) {
        g2.setColor(new Color(217, 217, 217));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
    }

    public void setPanelProperties() {

        model.setQuestionTypePanelWidth(model.getLoadQuestionBoxWidth());
        model.setQuestionTypePanelHeight(model.getLoadQuestionBoxHeight());

        setBounds(
                model.getLoadQuestionBoxX(), model.getLoadQuestionBoxY() + model.getLoadQuestionBoxHeight() + 10,
                model.getLoadQuestionBoxWidth(),
                model.getLoadQuestionBoxHeight()
        );

    }

    public void addComponentToPanel() {

        add(title);
        add(subtitle);

        for(JLabel label: questionTypeLabels) {
            add(label);
        }

        for(MyCheckBox checkBox: checkBoxes) {
            add(checkBox);
        }

    }

    public void handleEvent(){

        mcq.addItemListener(e -> model.setMCQ(mcq.isSelected()));
        trueFalse.addItemListener(e -> model.setTrueFalse(trueFalse.isSelected()));
        blank.addItemListener(e -> model.setBlank(blank.isSelected()));
        des.addItemListener(e -> model.setDes(des.isSelected()));

    }

    public void setLabels() {

        setTitleLabelFont(title);
        title.setBounds(10, 10, getWidth(), 50);

        setTextLabelFont(subtitle);
        subtitle.setBounds(10, 40, getWidth(), 50);

        int offsetY = 80;
        int i = 0;
        for(JLabel label: questionTypeLabels) {

            label.setBounds(50, offsetY, getWidth(), 50);
            label.setIcon(scaled[i]);
            label.setHorizontalTextPosition(JLabel.RIGHT);
            label.setHorizontalAlignment(JLabel.LEFT);
            setTextLabelFont(label);
            offsetY += 40;

            i++;
        }

    }

    public void setCheckBoxes() {

        int offsetY = 90;
        for(MyCheckBox checkBox:  checkBoxes) {

            checkBox.setBounds(10, offsetY, 30, 30);
            offsetY += 40;
        }

    }

    public void loadImages() {

        for(int i = 0; i < icons.length; i++) {
            icons[i] = new ImageIcon();
        }

        for(int i = 1; i <= scaled.length; i++) {

            java.net.URL imgUrl = getClass().getResource("/org/example/Image/QuestionType/" + i + ".png");

            if(imgUrl != null) {
                icons[i-1] = new ImageIcon(imgUrl);
            }else {
                String absPath = "D:\\learn\\LeetCode.com\\ExamGUI\\src\\main\\java\\org\\example\\Image\\QuestionType\\" + i + ".png";
                icons[i-1] = new ImageIcon(absPath);
            }

            if(icons[i - 1].getImage() != null) {
                scaled[i - 1] = new ImageIcon(icons[i - 1].getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
            }
            else{
                scaled[i - 1] = null;
            }
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
