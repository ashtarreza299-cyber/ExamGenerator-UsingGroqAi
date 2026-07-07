package org.example;


public class Model {


    private int panelWidth;
    private int panelHeight;
    private int sideBarWidth = 250;
    private int sideBarButtonsWidth = 200;
    private int sideBarButtonsHeight = 40;
    private int dashBoardWidth = 700;

    private int loadQuestionBoxX = 20;
    private int loadQuestionBoxY = 100;
    private int loadQuestionBoxWidth = 300;
    private int loadQuestionBoxHeight = 300;

    private int questionTypePanelWidth = 0;
    private int questionTypePanelHeight = 0;


    private int wordFileBoxX = 20;
    private int wordFileBoxY = 100;

    private int examSettingBoxWidth = 350;
    private int examSettingBoxHeight = 300;

    private int examSettingBoxX = 0;
    private int examSettingBoxY = 0;

    public int getPanelWidth(){
        return panelWidth;
    }

    public int getPanelHeight(){
        return panelHeight;
    }

    public void setPanelWidth(int panelWidth) {
        this.panelWidth = panelWidth;
    }

    public void setPanelHeight(int panelHeight) {
        this.panelHeight = panelHeight;
    }

    public int getSideBarWidth() {
        return sideBarWidth;
    }

    public int getSideBarButtonsWidth() {
        return sideBarButtonsWidth;
    }

    public void setSideBarButtonsWidth(int sideBarButtonsWidth) {
        this.sideBarButtonsWidth = sideBarButtonsWidth;
    }

    public int getSideBarButtonsHeight() {
        return sideBarButtonsHeight;
    }

    public void setSideBarButtonsHeight(int sideBarButtonsHeight) {
        this.sideBarButtonsHeight = sideBarButtonsHeight;
    }

    public int getDashBoardWidth() {
        return dashBoardWidth;
    }

    public void setDashBoardWidth(int dashBoardWidth) {
        this.dashBoardWidth = dashBoardWidth;
    }

    public int getLoadQuestionBoxWidth() {
        return loadQuestionBoxWidth;
    }

    public int getLoadQuestionBoxHeight() {
        return loadQuestionBoxHeight;
    }

    public int getLoadQuestionBoxY() {
        return loadQuestionBoxY;
    }

    public int getLoadQuestionBoxX() {
        return loadQuestionBoxX;
    }

    public int getWordFileBoxX() {
        return wordFileBoxX;
    }

    public int getWordFileBoxY() {
        return wordFileBoxY;
    }

    public void setWordFileBoxX(int x) {
        wordFileBoxX = x;
    }

    public void setWordFileBoxY(int y) {
        wordFileBoxY = y;
    }

    public int getExamSettingBoxWidth() {
        return examSettingBoxWidth;
    }

    public void setExamSettingBoxWidth(int examSettingBoxWidth) {
        this.examSettingBoxWidth = examSettingBoxWidth;
    }

    public int getExamSettingBoxHeight() {
        return examSettingBoxHeight;
    }

    public void setExamSettingBoxHeight(int examSettingBoxHeight) {
        this.examSettingBoxHeight = examSettingBoxHeight;
    }

    public int getExamSettingBoxX() {
        return examSettingBoxX;
    }

    public void setExamSettingBoxX(int examSettingBoxX) {
        this.examSettingBoxX = examSettingBoxX;
    }

    public int getExamSettingBoxY() {
        return examSettingBoxY;
    }

    public void setExamSettingBoxY(int examSettingBoxY) {
        this.examSettingBoxY = examSettingBoxY;
    }

    public int getQuestionTypePanelWidth() {
        return questionTypePanelWidth;
    }

    public void setQuestionTypePanelWidth(int questionTypePanelWidth) {
        this.questionTypePanelWidth = questionTypePanelWidth;
    }

    public int getQuestionTypePanelHeight() {
        return questionTypePanelHeight;
    }

    public void setQuestionTypePanelHeight(int questionTypePanelHeight) {
        this.questionTypePanelHeight = questionTypePanelHeight;
    }
}