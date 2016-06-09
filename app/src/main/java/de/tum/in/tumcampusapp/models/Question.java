package de.tum.in.tumcampusapp.models;


import android.text.TextUtils;

import java.util.ArrayList;

public class Question {

    private String question;
    private int id;
    private String text;
    private Boolean yes;
    private Boolean no;
    private String faculty;
    private Boolean flagged;
    private Boolean answered;
    private Boolean synced;
    private String[] facultyArr;

    public Question(String question, String text, Boolean yes, Boolean no, String faculties, Boolean flagged, Boolean answered, Boolean synced) {
        this.question = question;
        this.text = text;
        this.yes = yes;
        this.no = no;
        this.faculty = faculties;
        this.flagged = flagged;
        this.answered = answered;
        this.synced = synced;
    }

    // Const. for submiting Answeres for openQuestions
    public Question(String question, String text){
        this.question=question;
        this.text=text;
    }

    public Question(String text, ArrayList<String> faculties){
        this.text = text;
        this.faculty = TextUtils.join(",",faculties);
    }

    // For fetching OpenQuestions
    public Question(String question, String text, String[] faculties){
        this.question = question;
        this.text = text;
        this.facultyArr = faculties;
    }

    public String[] getFacultiesOfOpenQuestions() {
        return facultyArr;
    }

    public void setFacultiesOfOpenQuestions(String[] facultiesOfOpenQuestions) {
        this.facultyArr = facultiesOfOpenQuestions;
    }

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public String getQuestion() {return question;}

    public void setQuestion(String question) {this.text = question;}

    public Boolean getYes() {return yes;}

    public void setYes(Boolean yes) {this.yes = yes;}

    public Boolean getNo() {return no;}

    public void setNo(Boolean no) {this.no = no;}

    public String getText() {return text;}

    public void setText(String text) {this.text = text;}

    public String getFaculties() {return faculty;}

    public void setFaculties(String faculties) {this.faculty = faculties;}

    public Boolean getFlagged() {return flagged;}

    public void setFlagged(Boolean flagged) {this.flagged = flagged;}

    public Boolean getAnswered() {return answered;}

    public void setAnswered(Boolean answered) {this.answered = answered;}

    public Boolean getSynced() {return synced;}

    public void setSynced(Boolean synced) {this.synced = synced;}

    public String getFaculty() {return faculty;}

    public void setFaculty(String faculty) {this.faculty = faculty;}
}
