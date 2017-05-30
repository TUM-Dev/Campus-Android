package de.tum.`in`.tumcampusapp.models.tumcabe

class ChatRoom(var name: String?) {
    var id: Int = 0

    var members = -1

    override fun toString(): String {
        return id.toString() + ": " + name
    }
}
