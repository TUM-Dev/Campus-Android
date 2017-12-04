package de.tum.`in`.tumcampusapp.models.chat

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "unsent_chat_message")
data class UnsentChatMessageTable(@field:PrimaryKey
                             var _id: Int = -1,
                             var room: Int = -1,
                             var text: String = "",
                             var member: Int = -1,
                             var msg_id: Int = -1)
