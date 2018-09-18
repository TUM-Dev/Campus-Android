package de.tum.in.tumcampusapp.service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import java.util.List;

import de.tum.in.tumcampusapp.api.app.AuthenticationManager;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.component.ui.chat.ChatMessageViewModel;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;
import de.tum.in.tumcampusapp.component.ui.chat.repository.ChatMessageLocalRepository;
import de.tum.in.tumcampusapp.component.ui.chat.repository.ChatMessageRemoteRepository;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Utils;

import static de.tum.in.tumcampusapp.utils.Const.SEND_MESSAGE_SERVICE_JOB_ID;

/**
 * Service used to send chat messages.
 */
public class SendMessageService extends JobIntentService {

    public static final int MAX_SEND_TRIES = 5;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SendMessageService.class, SEND_MESSAGE_SERVICE_JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        TcaDb tcaDb = TcaDb.getInstance(this);

        ChatMessageRemoteRepository remoteRepository = ChatMessageRemoteRepository.INSTANCE;
        remoteRepository.setTumCabeClient(TUMCabeClient.getInstance(this));

        ChatMessageLocalRepository localRepository = ChatMessageLocalRepository.INSTANCE;
        localRepository.setDb(tcaDb);

        ChatMessageViewModel viewModel = new ChatMessageViewModel(localRepository, remoteRepository);
        viewModel.deleteOldEntries();

        List<ChatMessage> unsentMsg = viewModel.getUnsent();
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
                    viewModel.sendMessage(message.getRoom(), message, this.getApplicationContext());
                }

                return;
            } catch (NoPrivateKey noPrivateKey) {
                return; //Nothing can be done, just exit
            } catch (Exception e) {
                Utils.log(e);
                numberOfAttempts++;
            }

            // Sleep for five seconds, maybe the server is currently really busy
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Utils.log(e);
            }
        }
    }
}