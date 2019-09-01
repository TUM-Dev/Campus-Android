package de.tum.`in`.tumcampusapp.component.ui.chat

import android.content.Context
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavDestination
import de.tum.`in`.tumcampusapp.component.ui.chat.activity.ChatActivity
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoom
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoomDbRow
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager.CARD_CHAT
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import java.util.ArrayList

/**
 * Card that shows the cafeteria menu
 */
class ChatMessagesCard(
    context: Context,
    room: ChatRoomDbRow
) : Card(CARD_CHAT, context, "card_chat") {

    private var mUnread: List<ChatMessage> = ArrayList()
    private var nrUnread = 0
    private var mRoomName = ""
    private var mRoomId = 0
    private var mRoomIdString = ""

    private val chatMessageDao: ChatMessageDao

    override val optionsMenuResId: Int
        get() = R.menu.card_popup_menu

    init {
        val tcaDb = TcaDb.getInstance(context)
        chatMessageDao = tcaDb.chatMessageDao()
        setChatRoom(room.name, room.room, "${room.semesterId}:${room.name}")
    }

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        super.updateViewHolder(viewHolder)
        val chatMessagesViewHolder = viewHolder as? ChatMessagesCardViewHolder
        chatMessagesViewHolder?.bind(mRoomName, mRoomId, mRoomIdString, mUnread)
    }

    /**
     * Sets the information needed to build the card
     *
     * @param roomName Name of the chat room
     * @param roomId Id of the chat room
     */
    private fun setChatRoom(roomName: String, roomId: Int, roomIdString: String) {
        mRoomName = listOf("[A-Z, 0-9(LV\\.Nr)=]+$", "\\([A-Z]+[0-9]+\\)", "\\[[A-Z]+[0-9]+\\]")
                .map { it.toRegex() }
                .fold(roomName) { name, regex -> name.replace(regex, "") }
                .trim()
        chatMessageDao.deleteOldEntries()
        nrUnread = chatMessageDao.getNumberUnread(roomId)
        mUnread = chatMessageDao.getLastUnread(roomId).asReversed()
        mRoomIdString = roomIdString
        mRoomId = roomId
    }

    override fun getNavigationDestination(): NavDestination? {
        val bundle = Bundle().apply {
            val chatRoom = ChatRoom(mRoomIdString).apply { id = mRoomId }
            val value = Gson().toJson(chatRoom)
            putString(Const.CURRENT_CHAT_ROOM, value)
        }
        return NavDestination.Activity(ChatActivity::class.java, bundle)
    }

    override fun getId() = mRoomId

    override fun discard(editor: Editor) = chatMessageDao.markAsRead(mRoomId)

    companion object {

        @JvmStatic
        fun inflateViewHolder(
            parent: ViewGroup,
            interactionListener: CardInteractionListener
        ): CardViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_chat_messages, parent, false)
            return ChatMessagesCardViewHolder(view, interactionListener)
        }
    }
}
