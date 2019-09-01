package de.tum.`in`.tumcampusapp.component.tumui.feedback.model

data class FeedbackResult(
    var success: String = "",
    var error: String = ""
) {

    val isSuccess: Boolean
        get() = error.isEmpty() && success.isNotEmpty()
}
