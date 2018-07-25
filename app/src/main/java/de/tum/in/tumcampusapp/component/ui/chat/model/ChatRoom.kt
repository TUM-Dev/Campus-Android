package de.tum.`in`.tumcampusapp.component.ui.chat.model

data class ChatRoom(var name: String = "") {
    var id: Int = 0
    var members = -1

    override fun toString() = "$id: $name"

    fun getSemester() : String {
        if (name.contains(":")) {
            return name.substring(0, 3)
        }
        return "ZZZ"
    }

    fun getActualName() : String {
        if (name.contains(":")) {
            return name.substring(4)
        }
        return name
    }

    companion object {
        @JvmField val MODE_JOINED = 1
        @JvmField val MODE_UNJOINED = 0
    }

}
