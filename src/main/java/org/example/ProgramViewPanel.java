package org.example;

import javax.swing.*;

public abstract class ProgramViewPanel extends JPanel {

    public ProgramViewPanel(){
        setLayout(null);
    }

    abstract void createPanel();
}
