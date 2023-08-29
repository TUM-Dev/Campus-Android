package de.tum.`in`.tumcampusapp.component.other.general

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.other.generic.PushNotification
import de.tum.`in`.tumcampusapp.component.ui.alarm.model.FcmNotification
import de.tum.`in`.tumcampusapp.component.ui.overview.MainActivity
import de.tum.`in`.tumcampusapp.service.FcmReceiverService.Companion.UPDATE
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.tryOrNull

class UpdatePushNotification(
    payload: String,
    appContext: Context,
    notification: Int
) : PushNotification(appContext, UPDATE, notification, true) {

    private val data: FcmUpdate? = Gson().fromJson(payload, FcmUpdate::class.java)
    private val notificationFromServer: FcmNotification?
        get() = tryOrNull {
            TUMCabeClient.getInstance(appContext)
                .getNotification(notificationId)
        }

    init {
        // if (BuildConfig.VERSION_CODE < data.packageVersion) {
        // TODO(kordianbruck): self deactivate, if we actually send this packageVersion fom the server
        // }
    }

    /**
     * The displayed notification should only show one Update message
     */
    override val displayNotificationId = UPDATE

    override val notification: Notification?
        get() {
            data ?: return null
            if (data.sdkVersion > Build.VERSION.SDK_INT || BuildConfig.VERSION_CODE >= data.packageVersion) {
                return null
            }
            val info = notificationFromServer ?: return null

            val sound = Uri.parse("android.resource://${appContext.packageName}/${R.raw.message}")
            val alarm = Intent(appContext, MainActivity::class.java)
            val pending = PendingIntent.getActivity(appContext, 0, alarm, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val description = info.description.ifEmpty {
                String.format(appContext.getString(R.string.update_notification_description), data.releaseDate)
            }

            val title = info.title.ifEmpty { appContext.getString(R.string.update) }

            return NotificationCompat.Builder(appContext, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setSmallIcon(defaultIcon)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(description))
                .setContentText(description)
                .setContentIntent(pending)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setLights(-0xffff01, 500, 500)
                .setSound(sound)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(appContext, R.color.color_primary))
                .build()
        }
}
