package org.example.model;



import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Model {


    private int panelWidth;
    private int panelHeight;

    private int sideBarWidth = 250;
    private int sideBarButtonsWidth = 200;
    private int sideBarButtonsHeight = 40;
    private int dashBoardWidth = 700;

    private int dashBoardX;
    private int dashBoardY;


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

    private int distributionBoxX;
    private int distributionBoxWidth;
    private int distributionBoxHeight;

    private int numberOfStudents;
    private int numberOfQuestion;

    private boolean isShufflingOn;
    private boolean isOptionShufflingOn;
    private boolean isAnswerKeyIncluded;

    private boolean isMCQ;
    private boolean isDes;
    private boolean isBlank;
    private boolean isTrueFalse;
    private boolean drawGraph;

    private boolean mcqShuffled;
    private boolean desShuffled;
    private boolean trueFalseShuffled;
    private boolean blankSpaceShuffled;


    private File selectedFile;
    private File outputFolder;
    private String fileNaming;
    private int startNumber;

    private List<String> list = new ArrayList<>();

    private List<Question> questions = new ArrayList<>();
    private List<Question> des = new ArrayList<>();

    // Listeners to observe changes on the model
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<>();

    public void addModelListener(ModelListener l) {
        if (l != null) listeners.add(l);
    }

    public void removeModelListener(ModelListener l) {
        if (l != null) listeners.remove(l);
    }

    private void notifyListeners() {
        for (ModelListener l : listeners) {
            try { l.modelChanged(); } catch (Exception ignored) {}
        }
    }

    public int getPanelWidth(){
        return panelWidth;
    }

    public int getPanelHeight(){
        return panelHeight;
    }

    public void setPanelWidth(int panelWidth) {
        this.panelWidth = panelWidth;
        notifyListeners();
    }

    public void setPanelHeight(int panelHeight) {
        this.panelHeight = panelHeight;
        notifyListeners();
    }

    public int getSideBarWidth() {
        return sideBarWidth;
    }

    public int getSideBarButtonsWidth() {
        return sideBarButtonsWidth;
    }

    public void setSideBarButtonsWidth(int sideBarButtonsWidth) {
        this.sideBarButtonsWidth = sideBarButtonsWidth;
        notifyListeners();
    }

    public int getSideBarButtonsHeight() {
        return sideBarButtonsHeight;
    }

    public void setSideBarButtonsHeight(int sideBarButtonsHeight) {
        this.sideBarButtonsHeight = sideBarButtonsHeight;
        notifyListeners();
    }

    public int getDashBoardWidth() {
        return dashBoardWidth;
    }

    public void setDashBoardWidth(int dashBoardWidth) {
        this.dashBoardWidth = dashBoardWidth;
        notifyListeners();
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
        notifyListeners();
    }

    public void setWordFileBoxY(int y) {
        wordFileBoxY = y;
        notifyListeners();
    }

    public int getExamSettingBoxWidth() {
        return examSettingBoxWidth;
    }

    public void setExamSettingBoxWidth(int examSettingBoxWidth) {
        this.examSettingBoxWidth = examSettingBoxWidth;
        notifyListeners();
    }

    public int getExamSettingBoxHeight() {
        return examSettingBoxHeight;
    }

    public void setExamSettingBoxHeight(int examSettingBoxHeight) {
        this.examSettingBoxHeight = examSettingBoxHeight;
        notifyListeners();
    }

    public int getExamSettingBoxX() {
        return examSettingBoxX;
    }

    public void setExamSettingBoxX(int examSettingBoxX) {
        this.examSettingBoxX = examSettingBoxX;
        notifyListeners();
    }

    public int getExamSettingBoxY() {
        return examSettingBoxY;
    }

    public void setExamSettingBoxY(int examSettingBoxY) {
        this.examSettingBoxY = examSettingBoxY;
        notifyListeners();
    }

    public int getQuestionTypePanelWidth() {
        return questionTypePanelWidth;
    }

    public void setQuestionTypePanelWidth(int questionTypePanelWidth) {
        this.questionTypePanelWidth = questionTypePanelWidth;
        notifyListeners();
    }

    public int getQuestionTypePanelHeight() {
        return questionTypePanelHeight;
    }

    public void setQuestionTypePanelHeight(int questionTypePanelHeight) {
        this.questionTypePanelHeight = questionTypePanelHeight;
        notifyListeners();
    }

    public int getDistributionBoxX() {
        return distributionBoxX;
    }

    public void setDistributionBoxX(int distributionBoxX) {
        this.distributionBoxX = distributionBoxX;
        notifyListeners();
    }

    public int getDistributionBoxWidth() {
        return distributionBoxWidth;
    }

    public void setDistributionBoxWidth(int distributionBoxWidth) {
        this.distributionBoxWidth = distributionBoxWidth;
        notifyListeners();
    }

    public int getDistributionBoxHeight() {
        return distributionBoxHeight;
    }

    public void setDistributionBoxHeight(int distributionBoxHeight) {
        this.distributionBoxHeight = distributionBoxHeight;
        notifyListeners();
    }

    public boolean isShufflingOn() {
        return isShufflingOn;
    }

    public void setShufflingOn(boolean shufflingOn) {
        isShufflingOn = shufflingOn;
        notifyListeners();
    }

    public boolean isOptionShufflingOn() {
        return isOptionShufflingOn;
    }

    public void setOptionShufflingOn(boolean optionShufflingOn) {
        isOptionShufflingOn = optionShufflingOn;
        notifyListeners();
    }

    public boolean isAnswerKeyIncluded() {
        return isAnswerKeyIncluded;
    }

    public void setAnswerKeyIncluded(boolean answerKeyIncluded) {
        isAnswerKeyIncluded = answerKeyIncluded;
        notifyListeners();
    }

    public int getNumberOfStudents() {
        return numberOfStudents;
    }

    public void setNumberOfStudents(int numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
        notifyListeners();
    }

    public int getNumberOfQuestion() {
        return numberOfQuestion;
    }

    public void setNumberOfQuestion(int numberOfQuestion) {
        this.numberOfQuestion = numberOfQuestion;
        notifyListeners();
    }

    public boolean isMCQ() {
        return isMCQ;
    }

    public void setMCQ(boolean MCQ) {
        isMCQ = MCQ;
        notifyListeners();
    }

    public boolean isDes() {
        return isDes;
    }

    public void setDes(boolean des) {
        isDes = des;
        notifyListeners();
    }

    public boolean isBlank() {
        return isBlank;
    }

    public void setBlank(boolean blank) {
        isBlank = blank;
        notifyListeners();
    }

    public boolean isTrueFalse() {
        return isTrueFalse;
    }

    public void setTrueFalse(boolean trueFalse) {
        isTrueFalse = trueFalse;
        notifyListeners();
    }

    public String getFileNaming() {
        return fileNaming;
    }

    public void setFileNaming(String fileNaming) {
        this.fileNaming = fileNaming;
        notifyListeners();
    }

    public int getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(int startNumber) {
        this.startNumber = startNumber;
        notifyListeners();
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
        notifyListeners();
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
        notifyListeners();
    }

    public boolean isDrawGraph() {
        return drawGraph;
    }

    public void setDrawGraph(boolean drawGraph) {
        this.drawGraph = drawGraph;
        notifyListeners();
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
        notifyListeners();
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
        notifyListeners();
    }

    public boolean isMcqShuffled() {

        return mcqShuffled;
    }

    public boolean isDesShuffled() {
        return desShuffled;
    }

    public void setMcqShuffled(boolean mcqShuffled) {
        this.mcqShuffled = mcqShuffled;
        notifyListeners();
    }

    public void setDesShuffled(boolean desShuffled) {
        this.desShuffled = desShuffled;
        notifyListeners();
    }

    public boolean isTrueFalseShuffled() {
        return trueFalseShuffled;
    }

    public void setTrueFalseShuffled(boolean trueFalseShuffled) {
        this.trueFalseShuffled = trueFalseShuffled;
        notifyListeners();
    }

    public boolean isBlankSpaceShuffled() {
        return blankSpaceShuffled;
    }

    public void setBlankSpaceShuffled(boolean blankSpaceShuffled) {
        this.blankSpaceShuffled = blankSpaceShuffled;
        notifyListeners();
    }

    public int getDashBoardX() {
        return dashBoardX;
    }

    public void setDashBoardX(int dashBoardX) {
        this.dashBoardX = dashBoardX;
    }

    public int getDashBoardY() {
        return dashBoardY;
    }

    public void setDashBoardY(int dashBoardY) {
        this.dashBoardY = dashBoardY;
    }
}