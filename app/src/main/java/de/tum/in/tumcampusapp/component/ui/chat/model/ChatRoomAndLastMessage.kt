package de.tum.`in`.tumcampusapp.component.ui.chat.model

import androidx.room.ColumnInfo
import androidx.room.Embedded


class ChatRoomAndLastMessage {

    @Embedded
    var chatRoomDbRow: ChatRoomDbRow? = null

    var text: String? = null
    var timestamp: String? = null

    @ColumnInfo(name = "nr_unread")
    var nrUnread: Int = 0

    fun hasUnread() = nrUnread > 0

}