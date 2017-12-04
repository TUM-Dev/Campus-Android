package de.tum.`in`.tumcampusapp.models.chat

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * ChatMessage
 *
 * @param _id         Message ID
 * @param previous    Previous Message
 * @param room        Chatroom
 * @param text        Message Text
 * @param timestamp   Time message was send
 * @param signature
 * @param member      Chatuser
 * @param read        Read status
 * @param sending     Send status
 */
@Entity(tableName = "chat_message")
data class ChatMessageTable(@field:PrimaryKey
                       var _id: Int = -1,
                       var previous: Int = -1,
                       var room: Int = -1,
                       var text: String = "",
                       var timestamp: String = "",
                       var signature: String = "",
                       var member: Int = -1,
                       var read: Int = -1,
                       var sending: Int = -1)