package de.tum.`in`.tumcampusapp.component.ui.chat.model

import android.arch.persistence.room.Embedded


class ChatRoomAndLastMessage {
    @Embedded
    var chatRoomDbRow: ChatRoomDbRow? = null

    var text: String? = null
    var timestamp: String? = null
}