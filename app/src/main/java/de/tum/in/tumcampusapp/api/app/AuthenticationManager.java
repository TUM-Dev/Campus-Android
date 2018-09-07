package de.tum.in.tumcampusapp.api.app;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.UUID;

import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.api.app.exception.NoPublicKey;
import de.tum.in.tumcampusapp.api.app.model.DeviceRegister;
import de.tum.in.tumcampusapp.api.app.model.ObfuscatedIdsUpload;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeStatus;
import de.tum.in.tumcampusapp.api.app.model.UploadStatus;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeVerification;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
import de.tum.in.tumcampusapp.api.tumonline.model.TokenConfirmation;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.service.FcmIdentificationService;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.RSASigner;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This provides methods to authenticate this app installation with the tumcabe server and other instances requiring a pki.
 */
public class AuthenticationManager {
    private final static String ALGORITHM = "RSA";
    private final static int RSA_KEY_SIZE = 1024;
    private static String uniqueID;
    private final Context mContext;

    public AuthenticationManager(Context c) {
        mContext = c;
    }

    /**
     * Gets an unique id that identifies this device.
     * Should only reset after a reinstall or wiping of the settingsPrefix.
     *
     * @return Unique device id
     */
    public static synchronized String getDeviceID(Context context) {
        if (uniqueID == null) {
            uniqueID = Utils.getSetting(context, Const.PREF_UNIQUE_ID, "");
            if ("".equals(uniqueID)) {
                uniqueID = UUID.randomUUID()
                               .toString();
                Utils.setSetting(context, Const.PREF_UNIQUE_ID, uniqueID);
            }
        }
        return uniqueID;
    }

    public static KeyPairGenerator getKeyPairGeneratorInstance() {
        try {
            return KeyPairGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            // We don't support platforms without RSA
            throw new AssertionError(e);
        }
    }

    public static KeyFactory getKeyFactoryInstance() {
        try {
            return KeyFactory.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            // We don't support platforms without RSA
            throw new AssertionError(e);
        }
    }

    /**
     * Get the private key as string.
     *
     * @return
     * @throws NoPrivateKey
     */
    private String getPrivateKeyString() throws NoPrivateKey {
        String key = Utils.getSetting(mContext, Const.PRIVATE_KEY, "");
        if (key.isEmpty()) {
            throw new NoPrivateKey();
        }
        return key;
    }

    /**
     * Gets the public key as string.
     *
     * @return
     * @throws NoPublicKey
     */
    public String getPublicKeyString() throws NoPublicKey {
        String key = Utils.getSetting(mContext, Const.PUBLIC_KEY, "");
        if (key.isEmpty()) {
            throw new NoPublicKey();
        }
        return key;
    }

    /**
     * Loads the private key as an object.
     *
     * @return The private key object
     */
    private PrivateKey getPrivateKey() throws NoPrivateKey {
        byte[] privateKeyBytes = Base64.decode(this.getPrivateKeyString(), Base64.DEFAULT);
        try {
            return getKeyFactoryInstance().generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (InvalidKeySpecException e) {
            Utils.log(e);
        }
        return null;
    }

    /**
     * Sign a message with the currently stored private key.
     *
     * @param data String to be signed
     * @return signature used to verify this request
     * @throws NoPrivateKey
     */
    public String sign(String data) throws NoPrivateKey {
        RSASigner signer = new RSASigner(this.getPrivateKey());
        return signer.sign(data);
    }

    /**
     * Gets private key from preferences or generates one.
     *
     * @return true if a private key is present
     */
    public boolean generatePrivateKey(ChatMember member) {
        // Try to retrieve private key
        try {
            //Try to get the private key
            this.getPrivateKeyString();

            //Reupload it in the case it was not yet transmitted to the server
            this.uploadKey(this.getPublicKeyString(), member);

            // If we already have one don't create a new one
            return true;
        } catch (NoPrivateKey | NoPublicKey e) { //NOPMD
            //Otherwise catch a not existing private key exception and proceed generation
        }

        //Something went wrong, generate a new pair
        this.clearKeys();

        // If the key is not in shared preferences, a new generate key-pair
        KeyPair keyPair = generateKeyPair();

        //In order to store the preferences we need to encode them as base64 string
        String publicKeyString = keyToBase64(keyPair.getPublic()
                                                    .getEncoded());
        String privateKeyString = keyToBase64(keyPair.getPrivate()
                                                     .getEncoded());
        this.saveKeys(privateKeyString, publicKeyString);

        //New keys, need to re-upload
        this.uploadKey(publicKeyString, member);
        return true;
    }

    /**
     * Try to upload the public key to the server and remember that state.
     *
     * @param publicKey
     */
    private void uploadKey(String publicKey, final ChatMember member) {
        //If we already uploaded it we don't need to redo that
        if (Utils.getSettingBool(mContext, Const.PUBLIC_KEY_UPLOADED, false)) {
            this.tryToUploadFcmToken();
            return;
        }

        try {
            DeviceRegister dr = DeviceRegister.Companion.getDeviceRegister(mContext, publicKey, member);

            // Upload public key to the server
            TUMCabeClient.getInstance(mContext)
                         .deviceRegister(dr, new Callback<TUMCabeStatus>() {

                             @Override
                             public void onResponse(Call<TUMCabeStatus> call, Response<TUMCabeStatus> response) {
                                 //Remember that we are done, only if we have submitted with the member information
                                 if (response.isSuccessful() && "ok".equals(response.body()
                                                                                    .getStatus())) {
                                     Utils.setSetting(mContext, Const.PUBLIC_KEY_UPLOADED, true);

                                     AuthenticationManager.this.tryToUploadFcmToken();
                                 }
                             }

                             @Override
                             public void onFailure(Call<TUMCabeStatus> call, Throwable t) {
                                 Utils.log(t, "Failure uploading public key");
                                 Utils.setSetting(mContext, Const.PUBLIC_KEY_UPLOADED, false);
                             }
                         });
        } catch (NoPrivateKey noPrivateKey) {
            this.clearKeys();
        }
    }

    /**
     * Uploads the public key to TUMonline. Throws a {@link NoPublicKey} exception if the stored key
     * is empty.
     * @throws NoPublicKey Thrown if the stored key is empty.
     */
    public void uploadPublicKey() throws NoPublicKey {
        final String token = Utils.getSetting(mContext, Const.ACCESS_TOKEN, "");
        final String publicKey = Uri.encode(getPublicKeyString());

        TUMOnlineClient
                .getInstance(mContext)
                .uploadSecret(token, publicKey)
                .enqueue(new Callback<TokenConfirmation>() {
                    @Override
                    public void onResponse(@NonNull Call<TokenConfirmation> call,
                                           @NonNull Response<TokenConfirmation> response) {
                        TokenConfirmation confirmation = response.body();
                        if (confirmation != null && confirmation.isConfirmed()) {
                            Utils.log("Uploaded public key successfully");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TokenConfirmation> call, @NonNull Throwable t) {
                        Utils.log(t);
                        // TODO: We should probably try again
                    }
                });
    }

    public void tryToUploadFcmToken() {
        // Check device for Play Services APK. If check succeeds, proceed with FCM registration.
        // Can only be done after the public key has been uploaded
        if (Utils.getSettingBool(mContext, Const.PUBLIC_KEY_UPLOADED, false)
            && GoogleApiAvailability.getInstance()
                                    .isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS) {
            FcmIdentificationService idService = new FcmIdentificationService(mContext);
            idService.checkSetup();
        }
    }

    /**
     * synchronous method!
     * @param uploadStatus
     */
    public void uploadObfuscatedIds(UploadStatus uploadStatus) {
        String lrzId = Utils.getSetting(mContext, Const.LRZ_ID, "");
        if (lrzId.isEmpty()) {
            Utils.log("Can't upload obfuscated ids: no lrz id");
            return;
        }

        ObfuscatedIdsUpload upload;
        try {
            upload = new ObfuscatedIdsUpload(
                    "", "", "",
                    TUMCabeVerification.create(mContext, null)
            );
        } catch (NoPrivateKey noPrivateKey) {
            Utils.log(noPrivateKey, "Can't upload obfuscated ids: no private key");
            return;
        }

        String studentId = Utils.getSetting(mContext, Const.TUMO_STUDENT_ID, "");
        String employeeId = Utils.getSetting(mContext, Const.TUMO_EMPLOYEE_ID, "");
        String externalId = Utils.getSetting(mContext, Const.TUMO_EXTERNAL_ID, "");

        boolean doUpload = false;
        if (!uploadStatus.getStudentId() && !studentId.isEmpty()) {
            upload.setStudentId(studentId);
            doUpload = true;
        }
        if (!uploadStatus.getEmployeeId() && !employeeId.isEmpty()) {
            upload.setEmployeeId(employeeId);
            doUpload = true;
        }
        if (!uploadStatus.getExternalId() && !externalId.isEmpty()) {
            upload.setExternalId(externalId);
            doUpload = true;
        }

        if (doUpload) {
            Utils.log("uploading obfuscated ids: " + upload.toString());
            TUMCabeStatus status = TUMCabeClient.getInstance(mContext).uploadObfuscatedIds(lrzId, upload).blockingSingle();
            Utils.log("uplod obfuscated ids status: " + status.getStatus());
        }
    }

    /**
     * Convert a byte array to a more manageable base64 string to store it in the preferences.
     */
    private static String keyToBase64(byte[] key) {
        return Base64.encodeToString(key, Base64.DEFAULT);
    }

    /**
     * Generates a keypair with the given ALGORITHM & size
     */
    private static KeyPair generateKeyPair() {
        KeyPairGenerator keyGen = getKeyPairGeneratorInstance();
        keyGen.initialize(AuthenticationManager.RSA_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    /**
     * Save private key in shared preferences.
     */
    private void saveKeys(String privateKeyString, String publicKeyString) {
        Utils.setSetting(mContext, Const.PRIVATE_KEY, privateKeyString);
        Utils.setSetting(mContext, Const.PUBLIC_KEY, publicKeyString);
    }

    /**
     * Reset all keys generated - this should actually never happen other than when a token is reset.
     */
    public void clearKeys() {
        this.saveKeys("", "");
        Utils.setSetting(mContext, Const.PUBLIC_KEY_UPLOADED, false);
    }
}
