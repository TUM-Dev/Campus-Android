package de.tum.`in`.tumcampusapp.api.app

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Base64
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.api.app.exception.NoPublicKey
import de.tum.`in`.tumcampusapp.api.app.model.*
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.api.tumonline.model.TokenConfirmation
import de.tum.`in`.tumcampusapp.service.FcmTokenHandler
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.RSASigner
import de.tum.`in`.tumcampusapp.utils.Utils.getSetting
import de.tum.`in`.tumcampusapp.utils.Utils.getSettingBool
import de.tum.`in`.tumcampusapp.utils.Utils.log
import de.tum.`in`.tumcampusapp.utils.Utils.setSetting
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.inject.Inject

/**
 * This provides methods to authenticate this app installation with the tumcabe server and other instances requiring a pki.
 */
class AuthenticationManager @Inject constructor(private val mContext: Context) {
    /**
     * Get the private key as string.
     *
     * @return
     * @throws NoPrivateKey Thrown if the stored key is empty.
     */
    private val privateKeyString: String
        get() {
            val key = getSetting(mContext, Const.PRIVATE_KEY, "")
            if (key.isEmpty()) throw NoPrivateKey()
            return key
        }

    /**
     * Gets the public key as string.
     *
     * @return
     * @throws NoPublicKey Thrown if the stored key is empty.
     */
    private val publicKeyString: String
        get() {
            val key = getSetting(mContext, Const.PUBLIC_KEY, "")
            if (key.isEmpty()) throw NoPublicKey()
            return key
        }

    /**
     * Loads the private key as an object.
     *
     * @return The private key object
     * @throws NoPrivateKey Thrown if the stored key is empty.
     */
    private val privateKey: PrivateKey?
        get() {
            val privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT)
            try {
                return keyFactoryInstance.generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
            } catch (e: InvalidKeySpecException) {
                log(e)
            }
            return null
        }

    /**
     * Sign a message with the currently stored private key.
     *
     * @param data String to be signed
     * @return signature used to verify this request
     * @throws NoPrivateKey Thrown if the stored key is empty.
     */
    fun sign(data: String): String {
        val signer = RSASigner(privateKey!!)
        return signer.sign(data)!!
    }

    /**
     * Gets private key from preferences or generates one.
     */
    fun generatePrivateKey() {
        try {
            // Try to get the private key
            privateKeyString

            // Reupload it in the case it was not yet transmitted to the server
            uploadKey(publicKeyString)

            // If we already have one don't create a new one
            return
        }
        // Otherwise catch a not existing private key exception and proceed generation
        catch (e: NoPrivateKey) {
        } catch (e: NoPublicKey) {
        }

        // Something went wrong, generate a new pair
        clearKeys()

        // If the key is not in shared preferences, a new generate key-pair
        val keyPair = generateKeyPair()

        // In order to store the preferences we need to encode them as base64 string
        val publicKeyString = keyToBase64(keyPair.public.encoded)
        val privateKeyString = keyToBase64(keyPair.private.encoded)
        saveKeys(privateKeyString, publicKeyString)

        // New keys, need to re-upload
        uploadKey(publicKeyString)
    }

    /**
     * Try to upload the public key to the server and remember that state.
     *
     * @param publicKey Thrown if the stored key is empty.
     */
    private fun uploadKey(publicKey: String) {
        // If we already uploaded it we don't need to redo that
        if (getSettingBool(mContext, Const.PUBLIC_KEY_UPLOADED, false)) {
            tryToUploadFcmToken()
            return
        }
        try {
            val dr: DeviceRegister = DeviceRegister.Companion.getDeviceRegister(mContext, publicKey)

            // Upload public key to the server
            TUMCabeClient.getInstance(mContext)
                .deviceRegister(dr, object : Callback<TUMCabeStatus?> {
                    override fun onResponse(call: Call<TUMCabeStatus?>, response: Response<TUMCabeStatus?>) {
                        // Remember that we are done, only if we have submitted with the member information
                        val status: TUMCabeStatus? = response.body()
                        if (response.isSuccessful() && status != null && (status.status == "ok")) {
                            setSetting(mContext, Const.PUBLIC_KEY_UPLOADED, true)
                            tryToUploadFcmToken()
                        }
                    }

                    override fun onFailure(call: Call<TUMCabeStatus?>, t: Throwable) {
                        log(t, "Failure uploading public key")
                        setSetting(mContext, Const.PUBLIC_KEY_UPLOADED, false)
                    }
                })
        } catch (noPrivateKey: NoPrivateKey) {
            clearKeys()
        }
    }

    /**
     * Uploads the public key to TUMonline. Throws a [NoPublicKey] exception if the stored key
     * is empty.
     *
     * @throws NoPublicKey Thrown if the stored key is empty.
     */
    @Throws(NoPublicKey::class)
    fun uploadPublicKey() {
        val token = getSetting(mContext, Const.ACCESS_TOKEN, "")
        val publicKey = Uri.encode(publicKeyString)
        TUMOnlineClient
            .getInstance(mContext)
            .uploadSecret(token, publicKey)
            .enqueue(object : Callback<TokenConfirmation?> {
                override fun onResponse(
                    call: Call<TokenConfirmation?>,
                    response: Response<TokenConfirmation?>
                ) {
                    val confirmation: TokenConfirmation? = response.body()
                    if (confirmation != null && confirmation.isConfirmed) {
                        log("Uploaded public key successfully")
                    }
                }

                override fun onFailure(call: Call<TokenConfirmation?>, t: Throwable) {
                    log(t)
                    // TODO(thellmund): We should probably try again
                }
            })
    }

    fun tryToUploadFcmToken() {
        // Check device for Play Services APK. If check succeeds, proceed with FCM registration.
        // Can only be done after the public key has been uploaded
        if (getSettingBool(mContext, Const.PUBLIC_KEY_UPLOADED, false) &&
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS
        ) {
            FcmTokenHandler.checkSetup(mContext)
        }
    }

    /**
     * synchronous method!
     *
     * @param uploadStatus
     */
    @SuppressLint("CheckResult")
    fun uploadObfuscatedIds(uploadStatus: UploadStatus) {
        val lrzId = getSetting(mContext, Const.LRZ_ID, "")
        if (lrzId.isEmpty()) {
            log("Can't upload obfuscated ids: no lrz id")
            return
        }
        val verification: TUMCabeVerification? = TUMCabeVerification.create(mContext, null)
        if (verification == null) {
            log("Can't upload obfuscated ids: no private key")
            return
        }
        val upload = ObfuscatedIdsUpload("", "", "", verification)
        val studentId = getSetting(mContext, Const.TUMO_STUDENT_ID, "")
        val employeeId = getSetting(mContext, Const.TUMO_EMPLOYEE_ID, "")
        val externalId = getSetting(mContext, Const.TUMO_EXTERNAL_ID, "")
        var doUpload = false
        if (!uploadStatus.studentId && !studentId.isEmpty()) {
            upload.studentId = studentId
            doUpload = true
        }
        if (!uploadStatus.employeeId && !employeeId.isEmpty()) {
            upload.employeeId = employeeId
            doUpload = true
        }
        if (!uploadStatus.externalId && !externalId.isEmpty()) {
            upload.externalId = externalId
            doUpload = true
        }
        if (doUpload) {
            log("uploading obfuscated ids: $upload")
            TUMCabeClient.getInstance(mContext)
                .uploadObfuscatedIds(lrzId, upload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    Consumer { status: TUMCabeStatus -> log("Upload obfuscated IDs status: " + status.status) },
                    Consumer { obj: Throwable -> obj.printStackTrace() })
        }
    }

    /**
     * Save private key in shared preferences.
     */
    private fun saveKeys(privateKeyString: String, publicKeyString: String) {
        setSetting(mContext, Const.PRIVATE_KEY, privateKeyString.trim { it <= ' ' })
        setSetting(mContext, Const.PUBLIC_KEY, publicKeyString.trim { it <= ' ' })
    }

    /**
     * Reset all keys generated - this should actually never happen other than when a token is reset.
     */
    fun clearKeys() {
        saveKeys("", "")
        setSetting(mContext, Const.PUBLIC_KEY_UPLOADED, false)
    }

    companion object {
        private const val ALGORITHM = "RSA"
        private const val RSA_KEY_SIZE = 1024
        private var uniqueID: String? = null

        /**
         * Gets an unique id that identifies this device.
         * Should only reset after a reinstall or wiping of the settingsPrefix.
         *
         * @return Unique device id
         */
        @JvmStatic
        @Synchronized
        fun getDeviceID(context: Context): String {
            if (uniqueID == null) {
                uniqueID = getSetting(context, Const.PREF_UNIQUE_ID, "")
                if ("" == uniqueID) {
                    uniqueID = UUID.randomUUID()
                        .toString()
                    setSetting(context, Const.PREF_UNIQUE_ID, uniqueID!!)
                }
            }
            return uniqueID!!
        }

        // We don't support platforms without RSA
        val keyFactoryInstance: KeyFactory
            get() = try {
                KeyFactory.getInstance(ALGORITHM)
            } catch (e: NoSuchAlgorithmException) {
                // We don't support platforms without RSA
                throw AssertionError(e)
            }

        /**
         * Convert a byte array to a more manageable base64 string to store it in the preferences.
         */
        private fun keyToBase64(key: ByteArray): String {
            return Base64.encodeToString(key, Base64.DEFAULT)
        }

        /**
         * Generates a keypair with the given ALGORITHM & size
         */
        @Synchronized
        private fun generateKeyPair(): KeyPair {
            return try {
                val keyGen = KeyPairGenerator.getInstance(ALGORITHM)
                keyGen.initialize(RSA_KEY_SIZE)
                keyGen.generateKeyPair()
            } catch (e: NoSuchAlgorithmException) {
                // We don't support platforms without RSA
                throw AssertionError(e)
            }
        }
    }
}