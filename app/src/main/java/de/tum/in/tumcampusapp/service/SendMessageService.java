package de.tum.in.tumcampusapp.service;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import de.tum.in.tumcampusapp.App;
import de.tum.in.tumcampusapp.api.app.AuthenticationManager;
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;
import de.tum.in.tumcampusapp.component.ui.chat.repository.ChatMessageLocalRepository;
import de.tum.in.tumcampusapp.component.ui.chat.repository.ChatMessageRemoteRepository;
import de.tum.in.tumcampusapp.utils.Utils;

import static de.tum.in.tumcampusapp.utils.Const.SEND_MESSAGE_SERVICE_JOB_ID;

/**
 * Service used to send chat messages.
 */
public class SendMessageService extends JobIntentService {

    public static final int MAX_SEND_TRIES = 5;

    @Inject
    ChatMessageLocalRepository localRepository;

    @Inject
    ChatMessageRemoteRepository remoteRepository;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SendMessageService.class, SEND_MESSAGE_SERVICE_JOB_ID, work);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((App) getApplicationContext()).getAppComponent().inject(this);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        localRepository.deleteOldEntries();
        List<ChatMessage> unsentMsg = localRepository.getUnsent();
        if (unsentMsg.isEmpty()) {
            return;
        }

        int numberOfAttempts = 0;
        AuthenticationManager am = new AuthenticationManager(this);

        // Try to send the message 5 times
        while (numberOfAttempts < MAX_SEND_TRIES) {
            try {
                for (ChatMessage message : unsentMsg) {
                    // Generate signature and store it in the message
                    message.setSignature(am.sign(message.getText()));

                    // Send the message to the server
                    remoteRepository.sendMessage(message);
                }

                return;
            } catch (NoPrivateKey noPrivateKey) {
                Utils.log(noPrivateKey);
                return; // Nothing can be done, just exit
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