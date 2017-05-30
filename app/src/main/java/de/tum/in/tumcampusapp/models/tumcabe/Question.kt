package de.tum.`in`.tumcampusapp.models.tumcabe


import de.tum.`in`.tumcampusapp.auxiliary.Utils

/**
 * Question model class for communication with the API and needed constructors in the project.
 */
class Question {

    var question: String? = null
        private set
    var text: String? = null
    private var faculty: String = ""
    private var answer: Int = 0
    val facultiesOfOpenQuestions: Array<String>? = null
    var results: Array<Answer>? = null
    var created: String? = null
    var end: String? = null

    /**
     * Used in the help function for deleting flagged questions in SurveyManager

     * @param question
     */
    constructor(question: String) {
        this.question = question
    }

    /**
     * Used in setting collected openQuestions from the db in the surveyCard

     * @param question
     * *
     * @param text
     */
    constructor(question: String, text: String) {
        this.question = question
        this.text = text
    }

    /**
     * Used for syncing answered openQuestions with the server

     * @param question
     * *
     * @param answer
     */
    constructor(question: String, answer: Int) {
        this.question = question
        this.answer = answer
    }


    /**
     * Used for submitting ownQuestions to the server
     */
    constructor(text: String, faculties: List<String>) {
        this.text = text
        this.faculty = Utils.arrayListToString(faculties)

    }

    /**
     * Presents answers structure for the questions used in receiving the answers on ownQuesitons from server
     */
    class Answer(val answer: String, val votes: Int)
}
