package de.tum.`in`.tumcampusapp.service

import android.os.Bundle
import androidx.annotation.IntDef
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.other.general.UpdatePushNotification
import de.tum.`in`.tumcampusapp.component.other.generic.PushNotification
import de.tum.`in`.tumcampusapp.component.ui.alarm.AlarmPushNotification
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatPushNotification
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import org.jetbrains.anko.notificationManager
import java.io.IOException

/**
 * This `IntentService` does the actual handling of the FCM message.
 * `FcmBroadcastReceiver` (a `WakefulBroadcastReceiver`) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls `completeWakefulIntent()` to release the
 * wake lock.
 */
class FcmReceiverService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage?) {
        val data = message?.data ?: return
        Utils.log("Notification received: $data")

        // Legacy messages need to be handled - maybe some data is missing?
        if (!data.containsKey(PAYLOAD) || !data.containsKey("type")) {
            handleLegacyNotification(data)
            return
        }

        val notificationId = data["notificationId"]?.toInt() ?: return
        val type = data["type"]?.toInt() ?: return
        val payload = data[PAYLOAD] ?: return

        createPushNotificationOfType(type, notificationId, payload)?.let {
            postNotification(it)
            try {
                it.sendConfirmation()
            } catch (e: IOException) {
                Utils.log(e)
            }
        }
    }

    /**
     * Try to map the given parameters to a notification.
     * We handle type specific actions in the respective classes
     * See: https://github.com/TCA-Team/TumCampusApp/wiki/GCM-Message-format
     */
    private fun createPushNotificationOfType(
        type: Int,
        notificationId: Int,
        payload: String
    ): PushNotification? {
        // Apparently, using the service context can cause issues here:
        // https://stackoverflow.com/questions/48770750/strange-crash-when-starting-notification
        val appContext = applicationContext

        return when (type) {
            CHAT_NOTIFICATION -> ChatPushNotification.fromJson(payload, appContext, notificationId)
            UPDATE -> UpdatePushNotification(payload, appContext, notificationId)
            ALERT -> AlarmPushNotification(payload, appContext, notificationId)
            else -> {
                // Nothing to do, just confirm the retrieved notificationId
                try {
                    TUMCabeClient
                            .getInstance(this)
                            .confirm(notificationId)
                } catch (e: IOException) {
                    Utils.log(e)
                }
                null
            }
        }
    }

    /**
     * Try to support old notifications by matching it as a legacy chat notificationId
     * TODO(kordianbruck): do we still need this?
     */
    private fun handleLegacyNotification(data: Map<String, String>) {
        try {
            val bundle = Bundle().apply {
                data.entries.forEach { entry -> putString(entry.key, entry.value) }
            }
            ChatPushNotification.fromBundle(bundle, this, -1)?.also {
                postNotification(it)
            }
        } catch (e: Exception) {
            Utils.log(e)
        }
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        Utils.log("new FCM token received")
        Utils.setSetting(this, Const.FCM_INSTANCE_ID, FirebaseInstanceId.getInstance().id)
        Utils.setSetting(this, Const.FCM_TOKEN_ID, token ?: "")
    }

    /**
     * Post the corresponding local android notification for the received PushNotification (if any)
     * @param pushNotification the PushNotification containing a notification to post
     */
    private fun postNotification(pushNotification: PushNotification) {
        pushNotification.notification?.let {
            val manager = applicationContext.notificationManager
            manager.notify(pushNotification.displayNotificationId, it)
        }
    }

    companion object {
        /**
         * The possible Notification types
         */
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(CHAT_NOTIFICATION, UPDATE, ALERT)
        annotation class PushNotificationType

        const val CHAT_NOTIFICATION = 1
        const val UPDATE = 2
        const val ALERT = 3

        private const val PAYLOAD = "payload"
    }
}
