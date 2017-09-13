package de.tum.`in`.tumcampusapp.models.tumcabe

import android.content.Context

import java.math.BigInteger
import java.security.SecureRandom
import java.util.Date

import de.tum.`in`.tumcampusapp.auxiliary.AuthenticationManager
import de.tum.`in`.tumcampusapp.exceptions.NoPrivateKey

class DeviceRegister @Throws(NoPrivateKey::class)
constructor(c: Context, private val publicKey: String, member: ChatMember?) {

    private var signature: String? = null
    private val date: String
    private val rand: String
    private val device: String
    private var member: ChatMember? = null

    init {
        //Create some data
        this.date = Date().toString()
        this.rand = BigInteger(130, SecureRandom()).toString(32)
        this.device = AuthenticationManager.getDeviceID(c)

        //Sign this data for verification
        val am = AuthenticationManager(c)
        if (member == null) {
            this.signature = am.sign(date + rand + this.device)
        } else {
            this.member = member
            this.signature = am.sign(date + rand + this.device + this.member!!.lrzId + this.member!!.id)
        }
    }

}
