package de.tum.`in`.tumcampusapp.component.ui.alarm

import android.app.Notification
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.other.generic.PushNotification
import de.tum.`in`.tumcampusapp.component.ui.alarm.model.FcmAlert
import de.tum.`in`.tumcampusapp.component.ui.alarm.model.FcmNotification
import de.tum.`in`.tumcampusapp.service.FcmReceiverService.Companion.ALERT
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.RSASigner
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.tryOrNull
import java.security.InvalidKeyException
import java.security.PublicKey
import java.security.SignatureException
import java.security.spec.X509EncodedKeySpec

/**
 * TUM alerting system
 */
class AlarmPushNotification(
        payload: String,
        appContext: Context,
        notification: Int
) : PushNotification(appContext, ALERT, notification, true) {

    private val alert: FcmAlert = Gson().fromJson(payload, FcmAlert::class.java)
    private val notificationFromServer: FcmNotification?
        get() {
            return tryOrNull {
                TUMCabeClient.getInstance(appContext)
                        .getNotification(notificationId)
            }
        }

    private lateinit var info: FcmNotification

    override val displayNotificationId = ALERT

    override val notification: Notification?
        get() {
            if (alert.silent) {
                // Do nothing
                return null
            }
            info = notificationFromServer ?: return null

            if (!isValidSignature(info.title, info.description, info.signature)) {
                Utils.log("Received an invalid RSA signature")
                return null
            }

            // FcmNotification sound
            val sound = Uri.parse("android.resource://${appContext.packageName}/${R.raw.message}")
            val alarm = Intent(appContext, AlarmActivity::class.java).apply {
                putExtra("info", info)
                putExtra("alert", alert)
            }
            val pending = getActivity(appContext, 0, alarm, FLAG_UPDATE_CURRENT)
            // Strip any html tags from the description
            val strippedDescription = Utils.stripHtml(info.description)

            return NotificationCompat.Builder(appContext, Const.NOTIFICATION_CHANNEL_EMERGENCY)
                    .setSmallIcon(defaultIcon)
                    .setContentTitle(info.title)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(strippedDescription))
                    .setContentText(strippedDescription)
                    .setContentIntent(pending)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setLights(-0xffff01, 500, 500)
                    .setSound(sound)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(appContext, R.color.color_primary))
                    .build()
        }

    companion object {
        /**
         * This is the public key corresponding to the private key, which signs all messages sent by
         * the alarm system. It shall be used to verify that the sent message is correct
         */
        private const val PUB_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvSukueIrdowjJB/IHR6+tsCbYLF9kmC/2Sa8/kI9Ttq0aUyC0hDt2SBzuDDmp/RwnUap5/0xT/h3z+WgKOjrzWig4lmb7G2+RuuVn8466AErfp3YQVFiovNLGMqwfJzPZ9aV3sZBXCTeEbDkd/CLRp3kBYkAtL8NfIlbNaII9CWKdhS907JyEWRZO2DLiYLm37vK/hwg58eXHwL9jNYY3gFqGUlfWXwGC2a0yTOk9rgJejhUbU9GLWSL3OwiHVXlpPsvTC1Ry0H4kQQeisjCgpkPjOQAnAFRN9zZLtBZlIsssYvL3ohY/C1HfGzDwGTaELjhtzY9qHdFW/4GDZh8swIDAQAB"

        /**
         * Validates the signature sent to us
         *
         * @param title       the message title
         * @param description the message description
         * @param signature   the message signature
         * @return if the signature is valid
         */
        private fun isValidSignature(title: String, description: String, signature: String): Boolean {
            val key = cabePublicKey ?: return false

            val sig = RSASigner.getSignatureInstance()
            try {
                sig.initVerify(key)
            } catch (e: InvalidKeyException) {
                Utils.log(e)
                return false
            }

            val textBytes = "$title$description".toByteArray(Charsets.UTF_8)
            try {
                sig.update(textBytes)
            } catch (e: SignatureException) {
                Utils.log(e)
                return false
            }

            return try {
                sig.verify(Base64.decode(signature, Base64.DEFAULT))
            } catch (e: SignatureException) {
                Utils.log(e)
                false
            } catch (e: IllegalArgumentException) {
                Utils.log(e)
                false
            }
        }

        /**
         * converts the above base64 representation of the public key to a java object
         *
         * @return the public key of the TUMCabe server
         */
        private val cabePublicKey: PublicKey?
            get() {
                val keyFactory = AuthenticationManager.getKeyFactoryInstance()

                val keyBytes = Base64.decode(PUB_KEY, Base64.NO_WRAP)
                return tryOrNull {
                    keyFactory.generatePublic(X509EncodedKeySpec(keyBytes))
                }
            }
    }
}
