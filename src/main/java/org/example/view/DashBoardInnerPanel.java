package org.example.view;


import javax.swing.*;
import java.awt.*;

public interface DashBoardInnerPanel {


    void setTitleLabelFont(JLabel title);

    void setTextLabelFont(JLabel text);

    void paintComponent(Graphics g);
}
