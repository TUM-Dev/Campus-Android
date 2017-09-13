package de.tum.`in`.tumcampusapp.models.tumcabe

import android.content.Context
import de.tum.`in`.tumcampusapp.auxiliary.AuthenticationManager

class DeviceUploadGcmToken
constructor(c: Context, private val token: String) {

    private val verification: DeviceVerification
    private val signature: String

    init {
        this.verification = DeviceVerification(c)

        //Sign this data for verification
        val am = AuthenticationManager(c)
        this.signature = am.sign(token)
    }//Create some data

}
