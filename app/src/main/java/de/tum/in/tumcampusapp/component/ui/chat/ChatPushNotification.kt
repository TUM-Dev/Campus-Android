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
class ChatPushNotification(private val fcmChatPayload: FcmChat, context: Context, notification: Int) :
        PushNotification(context, CHAT_NOTIFICATION, notification, true) {

    private val passedChatRoom by lazy {
        tryOrNull {
            TUMCabeClient.getInstance(context)
                    .getChatRoom(fcmChatPayload.room)
        }
    }

    private val chatMessageDao by lazy {
        TcaDb.getInstance(context)
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

    /**
     * Create a legacy chat notification from a bundle
     */
    constructor(extras: Bundle, context: Context, notification: Int) :
            this(context = context, notification = notification, fcmChatPayload = FcmChat().apply {
                // Get the update details
                room = Integer.parseInt(extras.getString("room")!!)
                member = Integer.parseInt(extras.getString("member")!!)
                // Message part is only present if we have a updated message
                message = if (extras.containsKey("message")) {
                    Integer.parseInt(extras.getString("message")!!)
                } else {
                    -1
                }
            })

    /**
     * Create a chat notification from a json payload
     * @param payload JSON encoded FcmChat payload
     */
    constructor(payload: String, context: Context, notification: Int) :
            this(context = context, notification = notification,
                    fcmChatPayload = Gson().fromJson(payload, FcmChat::class.java))

    @SuppressLint("CheckResult")
    private fun getNewMessages(chatRoom: ChatRoom, messageId: Int) {
        val verification = TUMCabeVerification.create(context, null) ?: return

        ChatMessageLocalRepository.db = TcaDb.getInstance(context)
        ChatMessageRemoteRepository.tumCabeClient = TUMCabeClient.getInstance(context)
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
        val messages = chatMessageDao.getLastUnread(chatRoom.id)?.asReversed()
        val intent = Intent(Const.CHAT_BROADCAST_NAME).apply {
            putExtra("FcmChat", R.string.extras)
        }
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent)

        val notificationText = messages?.joinToString("\n")
        // Put the data into the intent
        val notificationIntent = Intent(context, ChatActivity::class.java).apply {
            putExtra(Const.CURRENT_CHAT_ROOM, Gson().toJson(chatRoom))
        }

        val taskStackBuilder = TaskStackBuilder.create(context).apply {
            addNextIntent(Intent(context, MainActivity::class.java))
            addNextIntent(Intent(context, ChatRoomsActivity::class.java))
            addNextIntent(notificationIntent)
        }

        showNotification(chatRoom, taskStackBuilder, notificationText)
    }

    /**
     * Show a nicely formatted notification, if applicable
     */
    private fun showNotification(chatRoom: ChatRoom, taskStackBuilder: TaskStackBuilder, text: String?) {
        // Check if chat is currently open then don't show a notificationId if it is
        if (ChatActivity.mCurrentOpenChatRoom != null
                && fcmChatPayload.room == ChatActivity.mCurrentOpenChatRoom.id) {
            return
        }

        if (!Utils.getSettingBool(context, "card_chat_phone", true) || fcmChatPayload.message != -1) {
            return
        }
        val contentIntent = taskStackBuilder.getPendingIntent(0, FLAG_UPDATE_CURRENT or FLAG_ONE_SHOT)

        // FcmNotification sound
        val sound = Uri.parse("android.resource://${context.packageName}/${R.raw.message}")

        /* TODO(jacqueline8711): Create the reply action and add the remote input
        String replyLabel = context.getResources().getString(R.string.reply_label);
        RemoteInput remoteInput = new RemoteInput.Builder(ChatActivity.EXTRA_VOICE_REPLY)
                .setLabel(replyLabel)
                .build();
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_reply,
                                                                                context.getString(R.string.reply_label),
                                                                                contentIntent)
                        .addRemoteInput(remoteInput)
                        .build();*/

        // Create a nice notificationId
        val n = NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_CHAT)
                .setSmallIcon(defaultIcon)
                .setLargeIcon(Utils.getLargeIcon(context, R.drawable.ic_chat_with_lines))
                .setContentTitle(chatRoom.name.substring(4))
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setLights(-0xffff01, 500, 500)
                .setSound(sound)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.color_primary))
                .build()

        context.notificationManager.notify(displayNotificationId, n)
    }

    companion object {
        private const val NOTIFICATION_ID = CardManager.CARD_CHAT
    }
}
