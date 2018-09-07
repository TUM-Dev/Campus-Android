package de.tum.`in`.tumcampusapp.api.app.model

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

data class TUMCabeVerification(
        val signature: String,
        val date: String,
        val rand: String,
        val id: String,
        var data: Any? = null
) {

    companion object {

        @JvmStatic
        @Throws(NoPrivateKey::class)
        fun create(context: Context, chatMember: ChatMember? = null): TUMCabeVerification {
            val date = Date().toString()
            val rand = BigInteger(130, SecureRandom()).toString(32)
            val id = chatMember?.id.toString() ?: AuthenticationManager.getDeviceID(context)

            val token = chatMember?.lrzId ?: id
            return TUMCabeVerification(
                    signature = AuthenticationManager(context).sign(date + rand + token),
                    date = date,
                    rand = rand,
                    id = id
            )
        }

    }

}