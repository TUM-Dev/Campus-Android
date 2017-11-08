package de.tum.`in`.tumcampusapp.models.tumcabe

data class ChatRoom(var name: String = "") {
    var id: Int = 0

    var members = -1

    override fun toString() = "$id: $name"
}
