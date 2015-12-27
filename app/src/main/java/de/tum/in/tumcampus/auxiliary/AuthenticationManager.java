package de.tum.in.tumcampus.auxiliary;

import android.content.Context;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.UUID;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.exception.NoPublicKey;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatPublicKey;
import de.tum.in.tumcampus.models.TUMCabeClient;
import retrofit.RetrofitError;

/**
 * This provides methods to authenticate this app installation with the tumcabe server and other instances requiring a pki
 */
public class AuthenticationManager {

    private static String uniqueID = null;
    private Context mContext;

    public AuthenticationManager(Context c) {
        mContext = c;
    }


    /**
     * Gets an unique id that identifies this device
     *
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
     * Gets the private key as string
     *
     * @return
     */
    private String getPrivateKeyString() throws NoPublicKey {
        String key = Utils.getInternalSettingString(mContext, Const.PRIVATE_KEY, "");
        if(key.isEmpty()){
            throw new NoPublicKey();
        }
        return key;
    }

    /**
     * Loads the private key as an object
     *
     * @return The private key object
     */
    private PrivateKey getPrivateKey() throws NoPublicKey {
        byte[] privateKeyBytes = Base64.decode(this.getPrivateKeyString(), Base64.DEFAULT);
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Utils.log(e);
        }
        return null;
    }

    public String sign(String data) throws NoPublicKey {
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
            String privateKeyString = this.getPrivateKeyString();

            // If we already have one don't create a new one
            return true;
        } catch (NoPublicKey noPublicKey) {}

        // If the key is not in shared preferences, generate key-pair
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair keyPair = keyGen.generateKeyPair();

            String publicKeyString = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT);
            String privateKeyString = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.DEFAULT);

            try {
                // Upload public key to the server
                TUMCabeClient.getInstance(mContext).uploadPublicKey(member.getId(), new ChatPublicKey(publicKeyString));

                // Save private key in shared preferences
                Utils.setInternalSetting(mContext, Const.PRIVATE_KEY, privateKeyString);
                Utils.setInternalSetting(mContext, Const.PRIVATE_KEY_ACTIVE, false); //We need to remember this state
                Utils.setInternalSetting(mContext, Const.PUBLIC_KEY, publicKeyString);
                return true;
            } catch (RetrofitError e) {
                Utils.log(e, "Failure uploading public key");
            }
        } catch (NoSuchAlgorithmException e) {
            Utils.log(e);
        }

        return false;
    }
}
