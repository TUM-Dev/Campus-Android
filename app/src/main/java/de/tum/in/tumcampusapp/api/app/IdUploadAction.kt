package de.tum.`in`.tumcampusapp.api.app

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.model.UploadStatus
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import javax.inject.Inject

/**
 * Asks to verify private key, uploads fcm token and obfuscated ids (if missing)
 */
class IdUploadAction @Inject constructor(
    private val context: Context,
    private val authManager: AuthenticationManager,
    private val tumCabeClient: TUMCabeClient
) : DownloadWorker.Action {

    override fun execute(cacheBehaviour: CacheControl) {
        val lrzId = Utils.getSetting(context, Const.LRZ_ID, "")

        val uploadStatus = tumCabeClient.getUploadStatus(lrzId) ?: return
        Utils.log("upload missing ids: " + uploadStatus.toString())

        // upload FCM Token if not uploaded or invalid
        if (uploadStatus.fcmToken != UploadStatus.UPLOADED) {
            Utils.log("upload fcm token")
            authManager.tryToUploadFcmToken()
        }

        if (lrzId.isEmpty()) {
            return // nothing else to be done
        }

        // ask server to verify our key
        if (uploadStatus.publicKey == UploadStatus.UPLOADED) { // uploaded but not verified
            Utils.log("ask server to verify key")
            val keyStatus = tumCabeClient.verifyKey()
            if (keyStatus?.status != UploadStatus.VERIFIED) {
                return // we can only upload obfuscated ids if we are verified
            }
        }

        // upload obfuscated ids
        authManager.uploadObfuscatedIds(uploadStatus)
    }
}
