package de.tum.`in`.tumcampusapp.api.app.model

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey

data class DeviceUploadFcmToken(var verification: DeviceVerification? = null,
                                var token: String = "",
                                var signature: String = "") {
    companion object {
        @Throws(NoPrivateKey::class)
        fun getDeviceUploadFcmToken(c: Context, token: String): DeviceUploadFcmToken {
            return DeviceUploadFcmToken(
                    verification = DeviceVerification.getDeviceVerification(c),
                    token = token,
                    signature = AuthenticationManager(c).sign(token)
            )
        }
    }
}
