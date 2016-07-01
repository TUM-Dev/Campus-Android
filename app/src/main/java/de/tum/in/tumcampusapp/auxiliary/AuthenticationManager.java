package de.tum.in.tumcampusapp.auxiliary;

import android.content.Context;
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

import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.exceptions.NoPublicKey;
import de.tum.in.tumcampusapp.models.ChatMember;
import de.tum.in.tumcampusapp.models.ChatPublicKey;
import de.tum.in.tumcampusapp.models.DeviceRegister;
import de.tum.in.tumcampusapp.models.TUMCabeClient;
import de.tum.in.tumcampusapp.models.TUMCabeStatus;
import de.tum.in.tumcampusapp.services.GcmIdentificationService;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * This provides methods to authenticate this app installation with the tumcabe server and other instances requiring a pki
 */
public class AuthenticationManager {
    private final static String algorithm = "RSA";
    private final static int rsaKeySize = 1024;
    private static String uniqueID = null;
    private final Context mContext;

    public AuthenticationManager(Context c) {
        mContext = c;
    }

    /**
     * Gets an unique id that identifies this device
     * should only reset after a reinstall or wiping of the settings
     * @return Unique device id
     */
    public static synchronized String getDeviceID(Context context) {
        if (uniqueID == null) {
            uniqueID = Utils.getInternalSettingString(context, Const.PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                Utils.setInternalSetting(context, Const.PREF_UNIQUE_ID, uniqueID);
            }
        }
        return uniqueID;
    }


    /**
     * Get the private key as string
     * @return
     * @throws NoPrivateKey
     */
    private String getPrivateKeyString() throws NoPrivateKey {
        String key = Utils.getInternalSettingString(mContext, Const.PRIVATE_KEY, "");
        if (key.isEmpty()) {
            throw new NoPrivateKey();
        }
        return key;
    }

    /**
     * Gets the public key as string
     * @return
     * @throws NoPublicKey
     */
    private String getPublicKeyString() throws NoPublicKey {
        String key = Utils.getInternalSettingString(mContext, Const.PUBLIC_KEY, "");
        if (key.isEmpty()) {
            throw new NoPublicKey();
        }
        return key;
    }

    /**
     * Loads the private key as an object
     *
     * @return The private key object
     */
    private PrivateKey getPrivateKey() throws NoPrivateKey {
        byte[] privateKeyBytes = Base64.decode(this.getPrivateKeyString(), Base64.DEFAULT);
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Utils.log(e);
        }
        return null;
    }

    /**
     * Sign a message with the currently stored private key
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
        if(this.generatePrivateKey()) {
            try {
                TUMCabeClient.getInstance(mContext).uploadPublicKey(member.getId(), new ChatPublicKey(this.getPublicKeyString()));
                return true;
            } catch (NoPublicKey noPublicKey) {
            } catch (RetrofitError e) {
            }
        }

        return false;
    }

    /**
     * Gets private key from preferences or generates one.
     *
     * @return true if a private key is present
     */
    public boolean generatePrivateKey() {
        // Try to retrieve private key
        try {
            //Try to get the private key
            this.getPrivateKeyString();

            //Reupload it in the case it was not yet transmitted to the server
            this.uploadKey(this.getPublicKeyString());

            // If we already have one don't create a new one
            return true;
        } catch (NoPrivateKey noPrivateKey) {
        } catch (NoPublicKey noPublicKey) {
        }

        //Something went wrong, generate a new pair
        this.clearKeys();

        // If the key is not in shared preferences, a new generate key-pair
        try {
            KeyPair keyPair = this.generateKeyPair();

            //In order to store the preferences we need to encode them as base64 string
            String publicKeyString = this.keyToBase64(keyPair.getPublic().getEncoded());
            String privateKeyString = this.keyToBase64(keyPair.getPrivate().getEncoded());
            this.saveKeys(privateKeyString, publicKeyString);

            //New keys, need to re-upload
            this.uploadKey(publicKeyString);
            return true;
        } catch (NoSuchAlgorithmException e) {
            Utils.log(e);
            this.clearKeys();
        }

        return false;
    }

    /**
     * Try to upload the public key to the server and remember that state
     * @param publicKey
     */
    private void uploadKey(String publicKey){
        //If we already uploaded it we don't need to redo that
        if(Utils.getInternalSettingBool(mContext, Const.PUBLIC_KEY_UPLOADED, false)){
            this.tryToUploadGcmToken();
            return;
        }

        try {
            DeviceRegister dr = new DeviceRegister(mContext, publicKey);

            // Upload public key to the server
            TUMCabeClient.getInstance(mContext).deviceRegister(dr, new Callback<TUMCabeStatus>() {

                @Override
                public void success(TUMCabeStatus s, Response response) {
                    Utils.log(s.getStatus());
                    Utils.log(response.getBody().toString());

                    //Remember that we are done
                    Utils.setInternalSetting(mContext, Const.PUBLIC_KEY_UPLOADED, true);

                    AuthenticationManager.this.tryToUploadGcmToken();
                }

                @Override
                public void failure(RetrofitError error) {
                    Utils.setInternalSetting(mContext, Const.PUBLIC_KEY_UPLOADED, false);
                }
            });
        } catch (RetrofitError e) {
            Utils.log(e, "Failure uploading public key");
            Utils.setInternalSetting(mContext, Const.PUBLIC_KEY_UPLOADED, false);
        } catch (NoPrivateKey noPrivateKey) {
            this.clearKeys();
        }
    }

    private void tryToUploadGcmToken(){
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        // Can only be done after the public key has been uploaded
        if (Utils.getInternalSettingBool(mContext, Const.PUBLIC_KEY_UPLOADED, false) && GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS) {
            GcmIdentificationService idService = new GcmIdentificationService(mContext);
            idService.checkSetup();
        }
    }

    /**
     * Convert a byte array to a more manageable base64 string to store it in the preferences
     * @param key
     * @return
     */
    private String keyToBase64(byte[] key){
        return Base64.encodeToString(key, Base64.DEFAULT);
    }

    /**
     * Generates a keypair with the given algorithm & size
     * @return
     * @throws NoSuchAlgorithmException
     */
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(AuthenticationManager.algorithm);
        keyGen.initialize(AuthenticationManager.rsaKeySize);
        return keyGen.generateKeyPair();
    }

    /**
     * Save private key in shared preferences
     */
    private void saveKeys(String privateKeyString, String publicKeyString) {
        Utils.setInternalSetting(mContext, Const.PRIVATE_KEY, privateKeyString);
        Utils.setInternalSetting(mContext, Const.PRIVATE_KEY_ACTIVE, false); //We need to remember this state in order to activate it later
        Utils.setInternalSetting(mContext, Const.PUBLIC_KEY, publicKeyString);
    }

    /**
     * Reset all keys generated - this should actually never happen
     */
    private void clearKeys(){
        this.saveKeys("","");
        Utils.setInternalSetting(mContext, Const.PUBLIC_KEY_UPLOADED, false);
    }
}
