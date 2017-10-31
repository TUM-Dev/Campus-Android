package de.tum.`in`.tumcampusapp.models.tumcabe

import android.content.Context
import de.tum.`in`.tumcampusapp.auxiliary.AuthenticationManager
import de.tum.`in`.tumcampusapp.exceptions.NoPrivateKey

data class DeviceUploadGcmToken(val verification: DeviceVerification, val token: String, val signature: String) {
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
