package de.tum.`in`.tumcampusapp.utils

import android.util.Base64
import java.security.*

/**
 * Class providing an API to generate signatures of strings.
 * Takes care of handling all unicode juggling and crypto algorithm selection.
 */
class RSASigner(
    /**
     * A [PrivateKey] instance which will be used to generate the signature.
     */
    private val privateKey: PrivateKey
) {

    /**
     * Sign the message given as the parameter and return it as a base64 encoded
     * [String].
     */
    fun sign(message: String): String? {
        val signer = signatureInstance

        try {
            signer.initSign(privateKey)
        } catch (e: InvalidKeyException) {
            Utils.log(e)
            return null
        }

        val messageBytes = message.toByteArray(Charsets.UTF_8)

        try {
            signer.update(messageBytes)
        } catch (e: SignatureException) {
            Utils.log(e)
            return null
        }

        val signature: ByteArray
        try {
            signature = signer.sign()
        } catch (e: SignatureException) {
            Utils.log(e)
            return null
        }

        return Base64.encodeToString(signature, Base64.DEFAULT)
    }

    companion object {

        // We don't support platforms without SHA1WithRSA
        val signatureInstance: Signature
            get() {
                val signature = "SHA1WithRSA"
                try {
                    return Signature.getInstance(signature)
                } catch (e: NoSuchAlgorithmException) {
                    throw AssertionError(e)
                }
            }
    }
}
