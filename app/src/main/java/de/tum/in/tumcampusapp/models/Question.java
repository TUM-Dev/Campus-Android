package de.tum.in.tumcampusapp.models;


import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Date;

public class Question {

    private String question;
    private String text;
    private String faculty;
    private int answer;
    private String[] facultyArr;
    private Answer[] results;
    private String created;
    private String end;

    // Const. for setting questions for the Survey Card
    public Question (String question, String text){
        this.question = question;
        this.text = text;
    }


    // Const. for submiting Answeres for openQuestions
    public Question(String question, int answer){
        this.question=question;
        this.answer=answer;
    }

    // Const. for fetching faculties
    public Question(String text, ArrayList<String> faculties){
        this.text = text;
        this.faculty = TextUtils.join(",",faculties);
    }


    public String[] getFacultiesOfOpenQuestions() {
        return facultyArr;
    }

    public String getQuestion() {return question;}

    public String getText() {return text;}

    public void setText(String text) {this.text = text;}

    public Answer[] getResults() {return results;}

    public void setResults(Answer[] results) {this.results = results;}

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public class Answer {
        private String answer;
        private int votes;

        public Answer(String answer, int votes){
            this.answer = answer;
            this.votes = votes;
        }

        public int getVotes() {
            return votes;
        }

        public String getAnswer() {
            return answer;
        }

    }
}
