package de.tum.`in`.tumcampusapp.component.ui.chat

import java.io.Serializable

/**
 * Used for parsing the FCM json payload for the 'chat' type
 */
data class FcmChat(var room: Int = 0,
                   var member: Int = 0,
                   var message: Int = 0) : Serializable {
    companion object {
        private const val serialVersionUID = -3920974316634829667L
    }
}
