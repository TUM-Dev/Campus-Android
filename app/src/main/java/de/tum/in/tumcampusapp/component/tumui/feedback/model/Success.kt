package de.tum.`in`.tumcampusapp.component.tumui.feedback.model

data class Success(
        var success: String = "",
        var error: String = "") {

    fun wasSuccessfullySent(): Boolean = error == "" && success != ""
}
