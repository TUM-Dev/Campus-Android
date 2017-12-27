package de.tum.in.tumcampusapp.services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dataAccessObjects.ChatMessageDao;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.managers.ChatMessageManager;
import de.tum.in.tumcampusapp.models.gcm.GCMChat;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;

import static de.tum.in.tumcampusapp.auxiliary.Const.SEND_MESSAGE_SERVICE_JOB_ID;

/**
 * Service used to silence the mobile during lectures
 */
public class SendMessageService extends JobIntentService {

    public static final int MAX_SEND_TRIES = 5;
    /**
     * Interval in milliseconds to check for current lectures
     */

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SendMessageService.class, SEND_MESSAGE_SERVICE_JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        TcaDb db = TcaDb.getInstance(getApplicationContext());
        ChatMessageDao chatMessageDao = db.chatMessageDao();
        chatMessageDao.deleteOldEntries();
        // Get all unsent messages from database
        List<ChatMessage> unsentMsg = chatMessageDao.getAllUnsent();
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
                    createdMessage.setSendingStatus(ChatMessage.STATUS_SENT);
                    chatMessageDao.deleteOldEntries();

                    if(createdMessage == null || createdMessage.getText() == null)  {
                        Utils.log("Message empty");
                        return;
                    }
                    boolean read = message.getMember().getId() == createdMessage.getMember().getId();
                    int status = chatMessageDao.getRead(createdMessage.getId());
                    if (status == 1)    {
                        read = true;
                    }
                    createdMessage.setSendingStatus(ChatMessage.STATUS_SENT);
                    createdMessage.setRead(read);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                    Date date;
                    try {
                        date = formatter.parse(createdMessage.getTimestamp());
                    } catch (ParseException e) {
                        date = new Date();
                    }
                    createdMessage.setTimestamp(Utils.getDateTimeString(date));
                    chatMessageDao.replaceMessage(createdMessage);
                    chatMessageDao.removeUnsentMessage(message.internalID);

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