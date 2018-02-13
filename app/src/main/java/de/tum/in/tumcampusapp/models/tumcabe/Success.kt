package de.tum.`in`.tumcampusapp.models.tumcabe

data class Success(
        var success: String = "",
        var error: String = "") {

    fun wasSuccessfullySent(): Boolean = error == "" && success != ""
}
