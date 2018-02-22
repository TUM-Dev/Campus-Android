package de.tum.`in`.tumcampusapp.api.app.model

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey

data class DeviceUploadGcmToken(var verification: DeviceVerification? = null,
                                var token: String = "",
                                var signature: String = "") {
    companion object {
        @Throws(NoPrivateKey::class)
        fun getDeviceUploadGcmToken(c: Context, token: String): DeviceUploadGcmToken {
            return DeviceUploadGcmToken(
                    verification = DeviceVerification.getDeviceVerification(c),
                    token = token,
                    signature = AuthenticationManager(c).sign(token)
            )
        }
    }
}
