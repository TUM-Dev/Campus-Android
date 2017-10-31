package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.managers.ChatMessageManager;
import de.tum.in.tumcampusapp.models.gcm.GCMChat;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;

/**
 * Service used to silence the mobile during lectures
 */
public class SendMessageService extends IntentService {

    public static final int MAX_SEND_TRIES = 5;
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
    protected void onHandleIntent(Intent intent) {
        // Get all unsent messages from database
        List<ChatMessage> unsentMsg = ChatMessageManager.getAllUnsentUpdated(this);
        if (unsentMsg.isEmpty()) {
            return;
        }

        int numberOfAttempts = 0;
        AuthenticationManager am = new AuthenticationManager(this);

        //Try to send the message 5 times
        while (numberOfAttempts < MAX_SEND_TRIES) {
            try {
                for (ChatMessage message : unsentMsg) {
                    // Generate signature and store it in the message
                    message.setSignature(am.sign(message.getText()));

                    // Send the message to the server
                    ChatMessage createdMessage;
                    if (message.getId() == 0) { //If the id is zero then its an new entry otherwise try to update it
                        createdMessage = TUMCabeClient.getInstance(this)
                                                      .sendMessage(message.getRoom(), message);
                        Utils.logv("successfully sent message: " + createdMessage.getText());
                    } else {
                        createdMessage = TUMCabeClient.getInstance(this)
                                                      .updateMessage(message.getRoom(), message);
                        Utils.logv("successfully updated message: " + createdMessage.getText());
                    }

                    //Update the status on the ui
                    createdMessage.setStatus(ChatMessage.STATUS_SENT);
                    ChatMessageManager messageManager = new ChatMessageManager(this, message.getRoom());
                    messageManager.replaceInto(createdMessage, message.getMember()
                                                                      .getId());
                    messageManager.removeFromUnsent(message);

                    // Send broadcast to eventually open ChatActivity
                    Intent i = new Intent("chat-message-received");
                    Bundle extras = new Bundle();
                    extras.putSerializable("GCMChat", new GCMChat(message.getRoom(), message.getMember()
                                                                                            .getId(), 0));
                    i.putExtras(extras);
                    LocalBroadcastManager.getInstance(this)
                                         .sendBroadcast(i);
                }

                //Exit the loop
                return;
            } catch (NoPrivateKey noPrivateKey) {
                return; //Nothing can be done, just exit
            } catch (IOException e) {
                Utils.log(e);
                numberOfAttempts++;
            }

            //Sleep for five seconds, maybe the server is currently really busy
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Utils.log(e);
            }
        }
    }
}