package de.tum.`in`.tumcampusapp.api.app.model

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

data class DeviceRegister(
    var signature: String = "",
    var date: String = "",
    var rand: String = "",
    var device: String = "",
    var publicKey: String = "",
    var member: ChatMember? = null
) {
    companion object {
        @Throws(NoPrivateKey::class)
        fun getDeviceRegister(c: Context, publickey: String, member: ChatMember?): DeviceRegister {
            val am = AuthenticationManager(c)
            val date = Date().toString()
            val rand = BigInteger(130, SecureRandom()).toString(32)
            val device = AuthenticationManager.getDeviceID(c)

            return DeviceRegister(date = date, rand = rand, device = device, publicKey = publickey, member = member,
                    signature =
                    if (member == null)
                        am.sign(date + rand + device)
                    else
                        am.sign(date + rand + device + member.lrzId + member.id)
            )
        }
    }
}
