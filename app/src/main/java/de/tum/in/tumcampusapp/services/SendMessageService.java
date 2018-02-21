package de.tum.in.tumcampusapp.services;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import java.util.List;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
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
        List<ChatMessage> unsentMsg = chatMessageViewModel.getUnsent();
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
                    chatMessageViewModel.sendMessage(message.getRoom(), message, this.getApplicationContext());
                    Utils.logv("successfully sent message: " + message.getText());
                }

                //Exit the loop
                return;
            } catch (NoPrivateKey noPrivateKey) {
                return; //Nothing can be done, just exit
            } catch (Exception e) {
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