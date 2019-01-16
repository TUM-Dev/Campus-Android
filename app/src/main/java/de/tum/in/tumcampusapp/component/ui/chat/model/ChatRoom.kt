package de.tum.`in`.tumcampusapp.component.ui.chat.model

import de.tum.`in`.tumcampusapp.R.string.name

data class ChatRoom(var title: String = "",
                    var semester: String = "ZZZ") {
    var id: Int = 0
    var members = -1

    constructor(combined: String) : this() {
        if (combined.contains(":")) {
            semester = combined.substring(0, 3)
            title = combined.substring(4)
        }
    }

    fun getCombinedName(): String {
        return "$semester:$title"
    }

    override fun toString() = "$id: $name"

    companion object {
        @JvmField
        val MODE_JOINED = 1
        @JvmField
        val MODE_UNJOINED = 0
    }

}
