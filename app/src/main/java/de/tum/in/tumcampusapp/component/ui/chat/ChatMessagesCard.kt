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
import com.google.common.collect.Lists
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.chat.activity.ChatActivity
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoom
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoomDbRow
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager.CARD_CHAT
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.component.ui.overview.card.NotificationAwareCard
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import java.util.*

/**
 * Card that shows the cafeteria menu
 */
class ChatMessagesCard(context: Context, room: ChatRoomDbRow) : NotificationAwareCard(CARD_CHAT, context, "card_chat") {
    private var mUnread: List<ChatMessage> = ArrayList<ChatMessage>()
    private var nrUnread = 0;
    private var mRoomName = ""
    private var mRoomId = 0
    private var mRoomIdString = ""

    private val chatMessageDao: ChatMessageDao

    init {
        val tcaDb = TcaDb.getInstance(context)
        chatMessageDao = tcaDb.chatMessageDao()
        setChatRoom(room.name, room.room, "${room.semesterId}:${room.name}")
    }

    override val title = mRoomName

    override fun  updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        mCard = viewHolder.itemView
        val card = viewHolder.itemView
        val cardsViewHolder = viewHolder as CardViewHolder
        val addedViews = cardsViewHolder.addedViews

        //Set title
        mTitleView = card.findViewById(R.id.card_title)
        val titleView = mTitleView

        if(nrUnread > 5){
            titleView!!.text = context.getString(R.string.card_message_title, mRoomName, nrUnread);
        } else {
            titleView!!.text = mRoomName
        }

        //Remove additional views
        mLinearLayout = card.findViewById(R.id.card_view)
        val linearLayout = mLinearLayout

        for (view in addedViews) {
            linearLayout!!.removeView(view)
        }

        // Show cafeteria menu
        mUnread.mapTo(addedViews) {
            addTextView(context.getString(R.string.card_message_line, it.member.displayName, it.text))
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
        nrUnread = chatMessageDao.getNumberUnread(roomId)
        mUnread = Lists.reverse(chatMessageDao.getLastUnread(roomId))
        mRoomIdString = roomIdString
        mRoomId = roomId
    }

    override fun getIntent() = Intent(context, ChatActivity::class.java).apply {
        putExtra(Const.CURRENT_CHAT_ROOM, Gson().toJson(ChatRoom(mRoomIdString).apply {
            id = mRoomId
        }))
        putExtras(Bundle())
    }


    override fun getId() = mRoomId

    override fun discard(editor: Editor) = chatMessageDao.markAsRead(mRoomId)

    override fun shouldShowNotification(prefs: SharedPreferences) = true

    override fun getRemoteViews(context: Context, appWidgetId: Int) = RemoteViews(context.packageName, R.layout.cards_widget_card).apply {
        setTextViewText(R.id.widgetCardTextView, title)
        setImageViewResource(R.id.widgetCardImageView, R.drawable.ic_comment)
    }

    companion object {
        fun inflateViewHolder(parent: ViewGroup) =
                CardViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.card_item, parent, false))
    }
}
