package de.tum.`in`.tumcampusapp.component.ui.chat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RemoteViews
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.chat.activity.ChatActivity
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoom
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoomDbRow
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager.CARD_CHAT
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.NotificationAwareCard
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import java.util.*

/**
 * Card that shows the cafeteria menu
 */
class ChatMessagesCard(context: Context, room: ChatRoomDbRow) : NotificationAwareCard(CARD_CHAT, context, "card_chat") {
    private val mUnread = ArrayList<ChatMessage>()
    private val chatMessageDao: ChatMessageDao
    private var mRoomName = ""
    private var mRoomId = 0
    private var mRoomIdString = ""

    init {
        val tcaDb = TcaDb.getInstance(context)
        chatMessageDao = tcaDb.chatMessageDao()
        setChatRoom(room.name, room.room, "${room.semesterId}:${room.name}")
    }

    override fun getTitle() = mRoomName

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        mCard = viewHolder.itemView
        val cardsViewHolder = viewHolder as Card.CardViewHolder
        val addedViews = cardsViewHolder.addedViews

        mLinearLayout = mCard.findViewById(R.id.card_view)
        mTitleView = mCard.findViewById(R.id.card_title)
        mTitleView.text = mRoomName

        //Remove additional views
        for (view in addedViews) {
            mLinearLayout.removeView(view)
        }

        // Show cafeteria menu
        mUnread.mapTo(addedViews) {
            addTextView("${it.member.displayName}: ${it.text}")
        }
    }

    /**
     * Sets the information needed to build the card
     *
     * @param roomName Name of the chat room
     * @param roomId   Id of the chat room
     */
    private fun setChatRoom(roomName: String, roomId: Int, roomIdString: String) {
        mRoomName = listOf("[A-Z, 0-9(LV\\.Nr)=]+$", "\\([A-Z]+[0-9]+\\)", "\\[[A-Z]+[0-9]+\\]")
                .map { it.toRegex() }
                .fold(roomName, { name, regex ->
                    name.replace(regex, "")
                })
                .trim()
        chatMessageDao.deleteOldEntries()
        //mUnread = chatMessageDao.getLastUnread(roomId); TODO
        mRoomIdString = roomIdString
        mRoomId = roomId
    }

    override fun getIntent() = Intent(mContext, ChatActivity::class.java).apply {
        putExtra(Const.CURRENT_CHAT_ROOM, Gson().toJson(ChatRoom(mRoomIdString).apply {
            id = mRoomId
        }))
        putExtras(Bundle())
    }


    override fun getId() = mRoomId

    override fun discard(editor: Editor) = chatMessageDao.markAsRead(id)

    override fun shouldShowNotification(prefs: SharedPreferences) = false

    override fun getRemoteViews(context: Context) = RemoteViews(context.packageName, R.layout.cards_widget_card).apply {
        setTextViewText(R.id.widgetCardTextView, title)
        setImageViewResource(R.id.widgetCardImageView, R.drawable.ic_comment)
    }

    companion object {
        fun inflateViewHolder(parent: ViewGroup) =
                Card.CardViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.card_item, parent, false))
    }
}
