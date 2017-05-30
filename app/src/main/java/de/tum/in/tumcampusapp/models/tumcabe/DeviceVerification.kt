package de.tum.`in`.tumcampusapp.models.tumcabe

import android.content.Context
import de.tum.`in`.tumcampusapp.auxiliary.AuthenticationManager
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

class DeviceVerification
constructor(c: Context) {

    private val signature: String
    private val date: String
    private val rand: String
    private val device: String

    init {
        //Create some data
        this.date = Date().toString()
        this.rand = BigInteger(130, SecureRandom()).toString(32)
        this.device = AuthenticationManager.getDeviceID(c)

        //Sign this data for verification
        val am = AuthenticationManager(c)
        this.signature = am.sign(date + rand + this.device)
    }

}
