package de.tum.in.tumcampusapp.component.ui.survey.model;

import java.util.List;

import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Question model class for communication with the API and needed constructors in the project.
 */
public class Question {

    private String question;
    private String text;
    private String faculty;
    private int answer;
    private String[] facultyArr;
    private Answer[] results;
    private String created;
    private String end;

    /**
     * Used in the help function for deleting flagged questions in SurveyManager
     *
     * @param question
     */
    public Question(String question) {
        this.question = question;
    }

    /**
     * Used in setting collected openQuestions from the db in the surveyCard
     *
     * @param question
     * @param text
     */
    public Question(String question, String text) {
        this.question = question;
        this.text = text;
    }

    /**
     * Used for syncing answered openQuestions with the server
     *
     * @param question
     * @param answer
     */
    public Question(String question, int answer) {
        this.question = question;
        this.answer = answer;
    }

    /**
     * Used for submitting ownQuestions to the server
     */
    public Question(String text, List<String> faculties) {
        this.text = text;
        this.faculty = Utils.arrayListToString(faculties);

    }

    public String[] getFacultiesOfOpenQuestions() {
        return facultyArr;
    }

    public String getQuestion() {
        return question;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Answer[] getResults() {
        return results;
    }

    public void setResults(Answer[] results) {
        this.results = results;
    }

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

    /**
     * Presents answers structure for the questions used in receiving the answers on ownQuesitons from server
     */
    public static class Answer {
        private final String answer;
        private final int votes;

        public Answer(String answer, int votes) {
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
