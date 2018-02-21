package de.tum.in.tumcampusapp.utils;

import android.util.Base64;

import com.google.common.base.Charsets;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

/**
 * Class providing an API to generate signatures of strings.
 * Takes care of handling all unicode juggling and crypto algorithm selection.
 */
public class RSASigner {
    /**
     * A {@link PrivateKey} instance which will be used to generate the signature.
     */
    private final PrivateKey privateKey;

    public RSASigner(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public static Signature getSignatureInstance() {
        String signature = "SHA1WithRSA";
        try {
            return Signature.getInstance(signature);
        } catch (NoSuchAlgorithmException e) {
            // We don't support platforms without SHA1WithRSA
            throw new AssertionError(e);
        }
    }

    /**
     * Sign the message given as the parameter and return it as a base64 encoded
     * {@link String}.
     *
     * @param message The message to be encoded
     * @return A base64 encoded signature
     */
    public String sign(String message) {
        Signature signer = getSignatureInstance();

        try {
            signer.initSign(privateKey);
        } catch (InvalidKeyException e) {
            Utils.log(e);
            return null;
        }

        byte[] messageBytes = message.getBytes(Charsets.UTF_8);

        try {
            signer.update(messageBytes);
        } catch (SignatureException e) {
            Utils.log(e);
            return null;
        }

        byte[] signature;
        try {
            signature = signer.sign();
        } catch (SignatureException e) {
            Utils.log(e);
            return null;
        }

        return Base64.encodeToString(
                signature,
                Base64.DEFAULT);
    }
}
