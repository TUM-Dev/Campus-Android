package de.tum.`in`.tumcampusapp.component.ui.chat

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeVerification
import de.tum.`in`.tumcampusapp.component.other.generic.PushNotification
import de.tum.`in`.tumcampusapp.component.ui.chat.activity.ChatActivity
import de.tum.`in`.tumcampusapp.component.ui.chat.activity.ChatRoomsActivity
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoom
import de.tum.`in`.tumcampusapp.component.ui.chat.repository.ChatMessageLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.chat.repository.ChatMessageRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.component.ui.overview.MainActivity
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.service.FcmReceiverService.Companion.CHAT_NOTIFICATION
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.tryOrNull
import org.jetbrains.anko.notificationManager

/**
 * Creates/modifies the notificationId when there is a new chat message.
 */
class ChatPushNotification(
    private val fcmChatPayload: FcmChat,
    appContext: Context,
    notification: Int
) : PushNotification(appContext, CHAT_NOTIFICATION, notification, true) {

    private val passedChatRoom by lazy {
        tryOrNull {
            TUMCabeClient.getInstance(appContext)
                    .getChatRoom(fcmChatPayload.room)
        }
    }

    private val chatMessageDao by lazy {
        TcaDb.getInstance(appContext)
                .chatMessageDao()
    }

    // we're showing the notificationId in the class itself because we have to load data first
    override val notification: Notification? = null

    /**
     * Chat messages are unique for each room, if messages from multiple rooms arrive, we should be
     * able to show all of them
     */
    override val displayNotificationId = (fcmChatPayload.room shl 4) + NOTIFICATION_ID

    init {
        Utils.logv("Received GCM notificationId: room=${fcmChatPayload.room} " +
                "member=${fcmChatPayload.member} message=${fcmChatPayload.message}")

        // Get the data necessary for the ChatActivity
        passedChatRoom?.let {
            getNewMessages(it, fcmChatPayload.message)
        }
    }

    @SuppressLint("CheckResult")
    private fun getNewMessages(chatRoom: ChatRoom, messageId: Int) {
        val verification = TUMCabeVerification.create(appContext, null) ?: return

        ChatMessageLocalRepository.db = TcaDb.getInstance(appContext)
        ChatMessageRemoteRepository.tumCabeClient = TUMCabeClient.getInstance(appContext)
        val chatMessageViewModel = ChatMessageViewModel(ChatMessageLocalRepository, ChatMessageRemoteRepository)

        if (messageId == -1) {
            chatMessageViewModel
                    .getNewMessages(chatRoom, verification)
                    .subscribe({ onDataLoaded() }, { Utils.log(it) })
            return
        }
        chatMessageViewModel
                .getOlderMessages(chatRoom, messageId.toLong(), verification)
                .subscribe({ /* Free ad space */ }, { Utils.log(it) })
    }

    /**
     * Receive chat message data from backend
     */
    private fun onDataLoaded() {
        val chatRoom = passedChatRoom ?: return
        val messages = chatMessageDao.getLastUnread(chatRoom.id)
        val intent = Intent(Const.CHAT_BROADCAST_NAME).apply {
            putExtra("FcmChat", R.string.extras)
        }
        LocalBroadcastManager.getInstance(appContext)
                .sendBroadcast(intent)
        val messagesText = messages?.asReversed()?.map { it.text }
        val notificationText = messagesText?.joinToString("\n")
        // Put the data into the intent
        val notificationIntent = Intent(appContext, ChatActivity::class.java).apply {
            putExtra(Const.CURRENT_CHAT_ROOM, Gson().toJson(chatRoom))
        }

        val taskStackBuilder = TaskStackBuilder.create(appContext).apply {
            addNextIntent(Intent(appContext, MainActivity::class.java))
            addNextIntent(Intent(appContext, ChatRoomsActivity::class.java))
            addNextIntent(notificationIntent)
        }

        showNotification(chatRoom, taskStackBuilder, notificationText)
    }

    /**
     * Show a nicely formatted notification, if applicable
     */
    private fun showNotification(chatRoom: ChatRoom, taskStackBuilder: TaskStackBuilder, text: String?) {
        // Check if chat is currently open then don't show a notificationId if it is
        if (ChatActivity.mCurrentOpenChatRoom != null &&
                fcmChatPayload.room == ChatActivity.mCurrentOpenChatRoom.id) {
            return
        }

        if (!Utils.getSettingBool(appContext, "card_chat_phone", true) || fcmChatPayload.message != -1) {
            return
        }
        val contentIntent = taskStackBuilder.getPendingIntent(0, FLAG_UPDATE_CURRENT or FLAG_ONE_SHOT)

        // FcmNotification sound
        val sound = Uri.parse("android.resource://${appContext.packageName}/${R.raw.message}")

        /* TODO(jacqueline8711): Create the reply action and add the remote input
        String replyLabel = appContext.getResources().getString(R.string.reply_label);
        RemoteInput remoteInput = new RemoteInput.Builder(ChatActivity.EXTRA_VOICE_REPLY)
                .setLabel(replyLabel)
                .build();
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_reply,
                                                                                appContext.getString(R.string.reply_label),
                                                                                contentIntent)
                        .addRemoteInput(remoteInput)
                        .build();*/

        // Create a nice notificationId
        val n = NotificationCompat.Builder(appContext, Const.NOTIFICATION_CHANNEL_CHAT)
                .setSmallIcon(defaultIcon)
                .setLargeIcon(Utils.getLargeIcon(appContext, R.drawable.ic_chat_with_lines))
                .setContentTitle(chatRoom.title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setLights(-0xffff01, 500, 500)
                .setSound(sound)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(appContext, R.color.color_primary))
                .build()

        appContext.notificationManager.notify(displayNotificationId, n)
    }

    companion object {
        private const val NOTIFICATION_ID = CardManager.CARD_CHAT

        /**
         * Create a legacy chat notification from a bundle
         */
        fun fromBundle(extras: Bundle, context: Context, notification: Int): ChatPushNotification? {
            val room = extras.getString("room")?.toIntOrNull() ?: return null
            val member = extras.getString("member")?.toIntOrNull() ?: return null
            // Message part is only present if we have a updated message
            val message = extras.getString("message")?.toIntOrNull() ?: -1

            return ChatPushNotification(appContext = context, notification = notification,
                    fcmChatPayload = FcmChat().apply {
                        this.room = room
                        this.member = member
                        this.message = message
                    })
        }

        /**
         * Create a chat notification from a json payload
         * @param payload JSON encoded FcmChat payload
         */
        fun fromJson(payload: String, context: Context, notification: Int): ChatPushNotification? {
            val fcmChatPayload = tryOrNull {
                Gson().fromJson(payload, FcmChat::class.java)
            } ?: return null
            return ChatPushNotification(appContext = context, notification = notification,
                    fcmChatPayload = fcmChatPayload)
        }
    }
}
