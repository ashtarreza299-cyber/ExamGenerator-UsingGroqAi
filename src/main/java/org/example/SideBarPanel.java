package org.example;

import javax.swing.*;
import java.awt.*;

public class SideBarPanel extends ProgramViewPanel{

    private Model model;

    private MyButton[] buttons = new MyButton[8];
    private ImageIcon[] icons = new ImageIcon[8];
    private ImageIcon[] scaled = new ImageIcon[8];

    private String[] btnText = {
            "DashBoard",
            "Load Questions",
            "Question Bank",
            "Generate Exams",
            "Students Groups",
            "Settings",
            "Help",
            "About"
    };

    public SideBarPanel(Model model) {

        this.model = model;
        setLayout(null);
        loadImages();
        createComponents();

    }

    public void loadImages() {

        for(int i = 0; i < icons.length; i++) {
            icons[i] = new ImageIcon();
        }

        for(int i = 1; i <= scaled.length; i++) {

            java.net.URL imgUrl = getClass().getResource("/org/example/Image/" + i + ".png");

            if(imgUrl != null) {
                icons[i-1] = new ImageIcon(imgUrl);
            }else {
                String absPath = "D:\\learn\\LeetCode.com\\ExamGUI\\src\\main\\java\\org\\example\\Image\\" + i + ".png";
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

    public void createButton(MyButton comp, String buttonText, ImageIcon icon, int offestY){

        comp.setText(buttonText);
        comp.setIcon(icon);

        comp.setBounds(
                20,
                offestY,
                model.getSideBarButtonsWidth(),
                model.getSideBarButtonsHeight()
        );

        add(comp);
    }

    public void createSideBarButtons() {

        for(int i = 0; i < buttons.length; i++)
            buttons[i] = new MyButton();

        int index = 0;
        int offsetY = 30;
        for(MyButton btn: buttons) {

            if(index == 0)
                createButton(btn, btnText[index], scaled[index], offsetY);
            else {
                offsetY += buttons[index - 1].getHeight() + 20;
                createButton(btn, btnText[index], scaled[index], offsetY);
            }
            index++;
        }

    }

    public void createComponents() {
        createPanel();
        createSideBarButtons();
    }

    @Override
    public void createPanel() {
        setBackground(new Color(0, 0, 102));
        setBounds(0, 0, model.getSideBarWidth(), model.getPanelHeight());
    }

}
