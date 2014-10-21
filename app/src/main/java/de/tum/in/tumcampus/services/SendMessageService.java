package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.RSASigner;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMessage;
import de.tum.in.tumcampus.models.managers.ChatMessageManager;
import retrofit.RetrofitError;

/**
 * Service used to silence the mobile during lectures
 */
public class SendMessageService extends IntentService {

    /**
     * Interval in milliseconds to check for current lectures
     */
    private static final String SEND_MESSAGE_SERVICE = "SendMessageService";

    /**
     * default init (run intent in new thread)
     */
    public SendMessageService() {
        super(SEND_MESSAGE_SERVICE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get all unsent messages from database
        ArrayList<ChatMessage> unsentMsg = ChatMessageManager.getAllUnsent(this);
        if (unsentMsg.size() == 0)
            return;

        // Initialize signer
        RSASigner signer = new RSASigner(getPrivateKeyFromSharedPrefs());

        int numberOfAttempts = 0;

        //Try to send the message 5 times
        while (numberOfAttempts < 5) {
            try {
                for (ChatMessage message : unsentMsg) {
                    // Generate signature
                    String signature = signer.sign(message.getText());
                    message.setSignature(signature);

                    // Send the message to the server
                    final ChatMessage createdMessage = ChatClient.getInstance(this).sendMessage(message.getRoom(), message);
                    Utils.logv("successfully sent message: " + createdMessage.getText() + "  (" + createdMessage.getId() + ")");
                    createdMessage.setStatus(ChatMessage.STATUS_SENT);

                    ChatMessageManager messageManager = new ChatMessageManager(this, message.getRoom());
                    messageManager.replaceInto(createdMessage, true);
                    messageManager.removeFromUnsent(message);

                    // Send broadcast to eventually open ChatActivity
                    Intent i = new Intent("chat-message-received");
                    Bundle extras = new Bundle();
                    extras.putString("room",""+message.getRoom());
                    extras.putBoolean("mine", true);
                    i.putExtras(extras);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                }

                //Exit the loop
                return;
            } catch (RetrofitError e) {
                Utils.log(e);
                numberOfAttempts++;
            }

            //Sleep for five seconds, maybe the server is currently really busy
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads the private key from preferences
     *
     * @return The private key object
     */
    private PrivateKey getPrivateKeyFromSharedPrefs() {
        String privateKeyString = Utils.getInternalSettingString(this, Const.PRIVATE_KEY, "");
        byte[] privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException e) {
            Utils.log(e);
        } catch (InvalidKeySpecException e) {
            Utils.log(e);
        }
        return null;
    }
}