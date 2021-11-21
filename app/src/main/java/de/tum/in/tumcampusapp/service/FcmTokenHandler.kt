package de.tum.`in`.tumcampusapp.service

import android.content.Context
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.model.DeviceUploadFcmToken
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeStatus
import de.tum.`in`.tumcampusapp.utils.Const.FCM_INSTANCE_ID
import de.tum.`in`.tumcampusapp.utils.Const.FCM_REG_ID_LAST_TRANSMISSION
import de.tum.`in`.tumcampusapp.utils.Const.FCM_REG_ID_SENT_TO_SERVER
import de.tum.`in`.tumcampusapp.utils.Const.FCM_TOKEN_ID
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.tryOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.Executors

object FcmTokenHandler {

    @JvmStatic
    fun checkSetup(context: Context) {
        val currentToken = Utils.getSetting(context, FCM_TOKEN_ID, "")

        // If we failed, we need to re register
        if (currentToken.isEmpty()) {
            registerInBackground(context)
        } else {
            // If the regId is not empty, we still need to check whether it was successfully sent to the
            // TCA server, because this can fail due to user not confirming their private key
            if (!Utils.getSettingBool(context, FCM_REG_ID_SENT_TO_SERVER, false)) {
                sendTokenToBackend(context, currentToken)
            }

            // Update the reg id in steady intervals
            checkRegisterIdUpdate(context, currentToken)
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     *
     *
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private fun registerInBackground(context: Context) {
        val executor = Executors.newSingleThreadExecutor()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = task.result
            Utils.setSetting(context, FCM_INSTANCE_ID, FirebaseInstallations.getInstance().id)
            Utils.setSetting(context, FCM_TOKEN_ID, token)

            // Reset the lock in case we are updating and maybe failed
            Utils.setSetting(context, FCM_REG_ID_SENT_TO_SERVER, false)
            Utils.setSetting(context, FCM_REG_ID_LAST_TRANSMISSION, Date().time)

            // Let the server know of our new registration ID
            sendTokenToBackend(context, token)

            Utils.log("FCM registration successful")
        }
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private fun sendTokenToBackend(context: Context, token: String?) {
        // Check if all parameters are present
        if (token == null || token.isEmpty()) {
            Utils.logv("Parameter missing for sending reg id")
            return
        }

        // Try to create the message
        val uploadToken = tryOrNull { DeviceUploadFcmToken.getDeviceUploadFcmToken(context, token) }
                ?: return

        TUMCabeClient
                .getInstance(context)
                .deviceUploadGcmToken(uploadToken, object : Callback<TUMCabeStatus> {
                    override fun onResponse(call: Call<TUMCabeStatus>, response: Response<TUMCabeStatus>) {
                        if (!response.isSuccessful) {
                            Utils.logv("Uploading FCM registration failed...")
                            return
                        }

                        val body = response.body() ?: return
                        Utils.logv("Success uploading FCM registration id: ${body.status}")

                        // Store in shared preferences the information that the GCM registration id
                        // was sent to the TCA server successfully
                        Utils.setSetting(context, FCM_REG_ID_SENT_TO_SERVER, true)
                    }

                    override fun onFailure(call: Call<TUMCabeStatus>, t: Throwable) {
                        Utils.log(t, "Failure uploading FCM registration id")
                        Utils.setSetting(context, FCM_REG_ID_SENT_TO_SERVER, false)
                    }
                })
    }

    /**
     * Helper function to check if we need to update the regid
     *
     * @param regId registration ID
     */
    private fun checkRegisterIdUpdate(context: Context, regId: String) {
        // Regularly (once a day) update the server with the reg id
        val lastTransmission = Utils.getSettingLong(context, FCM_REG_ID_LAST_TRANSMISSION, 0L)
        val now = Date()
        if (now.time - 24 * 3600000 > lastTransmission) {
            sendTokenToBackend(context, regId)
        }
    }
}
