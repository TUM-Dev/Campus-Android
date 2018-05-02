package de.tum.`in`.tumcampusapp.service

import android.content.Context
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.model.DeviceUploadGcmToken
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeStatus
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.tryOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*

class GcmIdentificationService(val context: Context? = null) : FirebaseInstanceIdService() {

    private val currentToken: String
        get() = Utils.getSetting(this.context, Const.GCM_TOKEN_ID, "")

    /**
     * Registers this phone with InstanceID and returns a valid token to be transmitted to the server
     *
     * @return String token that can be used to transmit messages to this client
     */
    @Throws(IOException::class)
    fun register(): String? {
        val instanceID = FirebaseInstanceId.getInstance()
        val token = instanceID.token
        Utils.setSetting(context, Const.GCM_INSTANCE_ID, instanceID.id)
        Utils.setSetting(context, Const.GCM_TOKEN_ID, token)
        return token
    }

    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Utils.setSetting(this, Const.GCM_TOKEN_ID, refreshedToken)
    }

    fun checkSetup() {
        // If we failed, we need to re register
        if (currentToken.isEmpty()) {
            registerInBackground()
        } else {
            // If the regId is not empty, we still need to check whether it was successfully sent to the
            // TCA server, because this can fail due to user not confirming their private key
            if (!Utils.getSettingBool(context, Const.GCM_REG_ID_SENT_TO_SERVER, false)) {
                sendTokenToBackend(currentToken)
            }

            // Update the reg id in steady intervals
            checkRegisterIdUpdate(currentToken)
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     *
     *
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private fun registerInBackground() {
        try {
            //Register a new ID
            val token = register()

            //Reset the lock in case we are updating and maybe failed
            Utils.setSetting(context, Const.GCM_REG_ID_SENT_TO_SERVER, false)
            Utils.setSetting(context, Const.GCM_REG_ID_LAST_TRANSMISSION, Date().time)

            // Let the server know of our new registration ID
            sendTokenToBackend(token)

            Utils.log("GCM registration successful")
        } catch (e: IOException) {
            Utils.log(e)
        }

    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private fun sendTokenToBackend(token: String?) {
        //Check if all parameters are present
        if (token == null || token.isEmpty()) {
            Utils.logv("Parameter missing for sending reg id")
            return
        }

        if (context == null) {
            return
        }

        // Try to create the message
        val uploadToken = tryOrNull { DeviceUploadGcmToken.getDeviceUploadGcmToken(context, token) } ?: return

        TUMCabeClient
                .getInstance(context)
                .deviceUploadGcmToken(uploadToken, object : Callback<TUMCabeStatus> {
                    override fun onResponse(call: Call<TUMCabeStatus>, response: Response<TUMCabeStatus>) {
                        if (!response.isSuccessful) {
                            Utils.logv("Uploading GCM registration failed...")
                            return
                        }

                        val body = response.body() ?: return
                        Utils.logv("Success uploading GCM registration id: " + body.status)

                        // Store in shared preferences the information that the GCM registration id
                        // was sent to the TCA server successfully
                        Utils.setSetting(context, Const.GCM_REG_ID_SENT_TO_SERVER, true)
                    }

                    override fun onFailure(call: Call<TUMCabeStatus>, t: Throwable) {
                        Utils.log(t, "Failure uploading GCM registration id")
                        Utils.setSetting(context, Const.GCM_REG_ID_SENT_TO_SERVER, false)
                    }
                })
    }

    /**
     * Helper function to check if we need to update the regid
     *
     * @param regId registration ID
     */
    private fun checkRegisterIdUpdate(regId: String) {
        // Regularly (once a day) update the server with the reg id
        val lastTransmission = Utils.getSettingLong(context, Const.GCM_REG_ID_LAST_TRANSMISSION, 0L)
        val now = Date()
        if (now.time - 24 * 3600000 > lastTransmission) {
            this.sendTokenToBackend(regId)
        }
    }

}
