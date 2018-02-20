package de.tum.`in`.tumcampusapp.models.dbEntities

import android.arch.persistence.room.Embedded
import de.tum.`in`.tumcampusapp.models.dbEntities.ChatRoomDbRow
import de.tum.`in`.tumcampusapp.models.tumcabe.ChatMessage


class ChatRoomAndLastMessage {
    @Embedded
    var chatRoomDbRow: ChatRoomDbRow? = null

    var text: String? = null
    var timestamp: String? = null
}