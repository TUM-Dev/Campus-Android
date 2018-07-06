package de.tum.`in`.tumcampusapp.component.ui.chat.model

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.IOException
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

        @Throws(NoPrivateKey::class)
        fun getChatVerification(c: Context, member: ChatMember, data: Any?): ChatVerification {
            val ret = this.getChatVerification(c, member)
            ret.data = data
            return ret
        }

        @Throws(IOException::class)
        fun createChatVerification(context: Context, data: Any?): ChatVerification {
            val currentChatMember = Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember::class.java)
            if(currentChatMember == null) {
                throw IOException("Failed to retrieve chat member!")
            }
            var chatVerification: ChatVerification? = null
            try {
                chatVerification = ChatVerification.getChatVerification(context, currentChatMember!!, data)
            } catch (noPrivateKey: NoPrivateKey) {
                noPrivateKey.printStackTrace()
                throw IOException()
            }

            return chatVerification
        }
    }
}
