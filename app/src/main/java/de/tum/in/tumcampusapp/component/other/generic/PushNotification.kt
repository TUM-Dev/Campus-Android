package de.tum.`in`.tumcampusapp.component.other.generic

import android.app.Notification
import android.content.Context
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.service.FcmReceiverService.Companion.PushNotificationType
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.IOException

/**
 * A generic push notification received via our backend server
 * @param appContext    application context
 * @param type          the concrete type ID of the notification
 * @param confirmation  if the notification needs to be confirmed to the backend
 */
abstract class PushNotification(protected val appContext: Context,
                                @PushNotificationType
                                protected val type: Int,
                                protected val notificationId: Int,
                                private val confirmation: Boolean) {
    protected val defaultIcon = R.drawable.ic_notification

    /**
     * A unique identifier for the displayed notification
     */
    abstract val displayNotificationId: Int

    /**
     * The android notification to show on the device
     */
    abstract val notification: Notification?

    /**
     * Send a confirmation to the backend, if it was requested
     */
    @Throws(IOException::class)
    fun sendConfirmation() {
        // Legacy support: notificationId id is -1 when old gcm messages arrive
        if (!confirmation || notificationId == -1) {
            return
        }
        Utils.logv("Confirmed notificationId $notificationId")
        TUMCabeClient.getInstance(appContext)
                .confirm(notificationId)
    }
}
