package de.tum.in.tumcampusapp.services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;

import java.util.List;

import de.tum.in.tumcampusapp.activities.ChatActivity;
import de.tum.in.tumcampusapp.adapters.ChatHistoryAdapter;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.managers.ChatMessageManager;
import de.tum.in.tumcampusapp.models.gcm.GCMChat;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
import de.tum.in.tumcampusapp.repository.ChatMessageLocalRepository;
import de.tum.in.tumcampusapp.repository.ChatMessageRemoteRepository;
import de.tum.in.tumcampusapp.viewmodel.ChatMessageViewModel;
import io.reactivex.disposables.CompositeDisposable;

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
        TcaDb tcaDb = TcaDb.getInstance(this);
        final CompositeDisposable mDisposable = new CompositeDisposable();
        ChatMessageRemoteRepository remoteRepository = ChatMessageRemoteRepository.INSTANCE;
        remoteRepository.setTumCabeClient(TUMCabeClient.getInstance(this));
        ChatMessageLocalRepository localRepository = ChatMessageLocalRepository.INSTANCE;
        localRepository.setDb(tcaDb);
        ChatMessageViewModel chatMessageViewModel = new ChatMessageViewModel(localRepository, remoteRepository, mDisposable);
        chatMessageViewModel.deleteOldEntries();

        // Get all unsent messages from database
        List<ChatMessage> unsentMsg = chatMessageViewModel.getAllUnsentList();
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
                        chatMessageViewModel.sendMessage(message.getRoom(), message);
                        Utils.logv("successfully sent message: " + message.getText());
                    } else {
                        chatMessageViewModel.updateMessage(message.getRoom(), message);

                        Utils.logv("successfully updated message: " + message.getText());
                    }

                   /* try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Utils.log(e);
                    }*/
                    //Update the status on the ui
                    chatMessageViewModel.deleteOldEntries();

                    chatMessageViewModel.removeUnsentMessage(message.internalID);

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
            } catch (Exception e) {
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