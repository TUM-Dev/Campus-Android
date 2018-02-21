package de.tum.`in`.tumcampusapp.models.tumcabe

import android.content.Context
import de.tum.`in`.tumcampusapp.auxiliary.AuthenticationManager
import de.tum.`in`.tumcampusapp.exceptions.NoPrivateKey
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

data class ChatVerification(var signature: String = "",
                            var date: String = "",
                            var rand: String = "",
                            var member: Int = 0,
                            var data: Any? = null) {
    companion object {
        @Throws(NoPrivateKey::class)
        fun getChatVerification(c: Context, member: ChatMember): ChatVerification {
            //Create some data
            val date = Date().toString()
            val rand = BigInteger(130, SecureRandom()).toString(32)
            val memberId = member.id

            //Sign this data for verification
            val am = AuthenticationManager(c)
            val signature = am.sign(date + rand + member.lrzId)

            return ChatVerification(
                    signature = signature,
                    date = date,
                    rand = rand,
                    member = memberId,
                    data = null
            )
        }
    }
}
