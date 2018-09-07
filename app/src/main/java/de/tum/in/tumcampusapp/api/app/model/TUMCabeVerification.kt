package de.tum.`in`.tumcampusapp.api.app.model

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

data class TUMCabeVerification(
        val signature: String,
        val date: String,
        val rand: String,
        val id: String,
        var data: Any? = null) {

    companion object {

        @JvmStatic
        fun createDeviceVerification(context: Context, data: Any? = null): TUMCabeVerification? {
            val id = AuthenticationManager.getDeviceID(context)
            return create(context, id, id, data)
        }

        @JvmStatic
        fun createMemberVerification(context: Context, data: Any? = null): TUMCabeVerification? {
            val chatMember = Utils.getSetting(
                    context, Const.CHAT_MEMBER, ChatMember::class.java) ?: return null

            val id = chatMember.id.toString()
            val token = chatMember.lrzId.orEmpty()

            return create(context, id, token, data)
        }

        @JvmStatic
        private fun create(context: Context,
                           id: String, token: String, data: Any? = null): TUMCabeVerification? {
            val date = Date().toString()
            val rand = BigInteger(130, SecureRandom()).toString(32)

            val signature = try {
                AuthenticationManager(context).sign(date + rand + id)
            } catch (e: NoPrivateKey) {
                return null
            }

            return TUMCabeVerification(signature, date, rand, id, data)
        }

    }

}