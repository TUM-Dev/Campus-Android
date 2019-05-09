package de.tum.`in`.tumcampusapp.component.ui.chat

import android.view.View
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import kotlinx.android.synthetic.main.card_chat_messages.view.*

class ChatMessagesCardViewHolder(
        itemView: View,
        interactionListener: CardInteractionListener
) : CardViewHolder(itemView, interactionListener) {

    @Suppress("UNUSED_PARAMETER")
    fun bind(roomName: String, roomId: Int, roomIdStr: String, unreadMessages: List<ChatMessage>) {
        with(itemView) {
            chatRoomNameTextView.text = if (unreadMessages.size > 5) {
                context.getString(R.string.card_message_title, roomName, unreadMessages.size)
            } else {
                roomName
            }

            if (contentContainerLayout.childCount == 0) {
                // We have not yet inflated the chat messages
                unreadMessages.asSequence()
                        .map { message ->
                            val memberName = message.member.displayName
                            context.getString(R.string.card_message_line, memberName, message.text)
                        }
                        .map { messageText ->
                            TextView(context, null, R.style.CardBody).apply {
                                text = messageText
                            }
                        }
                        .toList()
                        .forEach { textView ->
                            contentContainerLayout.addView(textView)
                        }
            }
        }
    }

}