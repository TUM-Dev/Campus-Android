package de.tum.in.tumcampusapp.component.ui.chat;

import android.util.Base64;


import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.AuthenticationManager;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatPublicKey;
import de.tum.in.tumcampusapp.utils.RSASigner;
import de.tum.in.tumcampusapp.utils.Utils;
import kotlin.text.Charsets;

/**
 * A class allowing a convenient API to check the validity of ChatMessages based
 * on a set of known public keys.
 * <p/>
 * The public keys against which the messages are to be validated need to be injected
 * at construct-time.
 */
public class ChatMessageValidator {

    /**
     * A list of public keys to be used to validate given chat messages.
     */
    private final List<ChatPublicKey> chatPublicKeys;

    /**
     * A list holding the given chat public keys converted to their {@link PublicKey}
     * instance.
     * <p/>
     * This list is constructed lazily, i.e. at the point when it is first required.
     */
    private List<PublicKey> publicKeys;

    /**
     * Constructs a new validator for the given public keys.
     *
     * @param publicKeys public key
     */
    public ChatMessageValidator(List<ChatPublicKey> publicKeys) {
        this.chatPublicKeys = publicKeys;
    }

    /**
     * Private helper method which performs the actual validation of a signature
     * attached to a particular text, given a {@link PublicKey}
     *
     * @param text      The text the signature has signed
     * @param signature The signature encoded as a base64 string
     * @param key       a {@link PublicKey} instance to use to verify the signature
     * @return Returns true if signature is valid
     */
    private static boolean verifySignature(String text, String signature,
                                           PublicKey key) {

        Signature sig = RSASigner.Companion.getSignatureInstance();

        try {
            sig.initVerify(key);
        } catch (InvalidKeyException e) {
            Utils.log(e);
            return false;
        }

        byte[] textBytes = text.getBytes(Charsets.UTF_8);

        try {
            sig.update(textBytes);
        } catch (SignatureException e) {
            Utils.log(e);
            return false;
        }
        try {
            return sig.verify(decodeByteRepresentation(signature));
        } catch (SignatureException e) {
            Utils.log(e);
            return false;
        }
    }

    /**
     * Private helper method which converts the public key representation returned by the
     * TCA endpoint to a {@link PublicKey} instance.
     *
     * @param chatPublicKey A model representing a TCA public key
     * @return {@link PublicKey} which corresponds to the given TCA public key
     */
    private static PublicKey convertToPublicKey(ChatPublicKey chatPublicKey) {
        // Base64 string -> Bytes
        byte[] keyBytes = decodeByteRepresentation(chatPublicKey.getKey());
        if (keyBytes == null) {
            return null;
        }
        KeyFactory keyFactory = AuthenticationManager.getKeyFactoryInstance();

        // Bytes -> PublicKey
        try {
            return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (InvalidKeySpecException e) {
            Utils.log(e);
            return null;
        }

    }

    /**
     * Private helper method decoding the given key text from its Base64 encoded
     * representation into an array of bytes.
     *
     * @param text The text to be decoded as a String
     * @return An array of bytes representing the given text
     */
    private static byte[] decodeByteRepresentation(String text) {
        if (text == null) {
            return null;
        }
        return Base64.decode(text.trim(), Base64.DEFAULT);
    }

    /**
     * Perform the validation of the signature attached to the given chat message
     * instance.
     * <p/>
     * The validation is performed against the list of public keys given to the validator
     * instance at construction-time.
     *
     * @param message The message to be validated
     * @return True if the signature is valid, False otherwise
     */
    public boolean validate(ChatMessage message) {
        if (publicKeys == null) {
            this.generatePublicKeys();
        }
        // Generate hash of the received message
        String text = message.getText();
        String signature = message.getSignature();

        // Try validating the message using any of the known public keys
        // If any of them succeed, the message is valid
        for (PublicKey key : publicKeys) {
            if (verifySignature(text, signature, key)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Private helper method which builds a list of valid {@link PublicKey}
     * instances based on the given list of TCA public keys.
     */
    private void generatePublicKeys() {
        publicKeys = new ArrayList<>();

        for (ChatPublicKey chatPublicKey : chatPublicKeys) {
            PublicKey key = convertToPublicKey(chatPublicKey);
            if (key != null) {
                publicKeys.add(key);
            }
        }
    }

}
