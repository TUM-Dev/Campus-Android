package de.tum.`in`.tumcampusapp.models.tumcabe

import android.content.Context
import de.tum.`in`.tumcampusapp.auxiliary.AuthenticationManager
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

class ChatVerification
constructor(c: Context, member: ChatMember) {

    var signature: String? = null
    private val date: String
    private val rand: String
    private val member: Int
    private var data: Any? = null

    init {
        //Create some data
        this.date = Date().toString()
        this.rand = BigInteger(130, SecureRandom()).toString(32)
        this.member = member.id

        //Sign this data for verification
        val am = AuthenticationManager(c)
        this.signature = am.sign(date + rand + member.lrzId)
    }

    fun setData(o: Any) {
        this.data = o
    }
}
