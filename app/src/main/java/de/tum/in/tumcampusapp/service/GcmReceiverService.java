/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tum.in.tumcampusapp.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.Map;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.other.general.Update;
import de.tum.in.tumcampusapp.component.other.generic.GenericNotification;
import de.tum.in.tumcampusapp.component.ui.alarm.AlarmNotification;
import de.tum.in.tumcampusapp.component.ui.chat.ChatNotification;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmReceiverService extends FirebaseMessagingService {

    private static final String PAYLOAD = "payload";

    /**
     * Called when message is received.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage message) {
        Utils.log("Notification received...");
        Map<String, String> data = message.getData();
        //Legacy messages need to be handled - maybe some data is missing?
        if (data.containsKey(PAYLOAD) && data.containsKey("type")) {
            //Get some important values
            int notification = Integer.parseInt(data.get("notification"));
            int type = Integer.parseInt(data.get("type"));
            String payload = data.get(PAYLOAD);

            //Initialize our outputs
            GenericNotification n = null;
            Utils.log("Notification received: " + data);

            //switch on the type as both the type and payload must be present
            switch (type) { //https://github.com/TCA-Team/TumCampusApp/wiki/GCM-Message-format
                case 1: //ChatNotification
                    n = new ChatNotification(payload, this, notification);
                    break;
                case 2: //Update
                    n = new Update(payload, this, notification);
                    break;
                case 3: //Alert
                    n = new AlarmNotification(payload, this, notification);
                    break;
                case 0: //Nothing to do, just confirm the retrieved notification
                default:
                    try {
                        TUMCabeClient.getInstance(this)
                                     .confirm(notification);
                    } catch (IOException e) {
                        Utils.log(e);
                    }
                    break;
            }

            //Post & save the notification if it was of any significance
            if (n != null) {
                this.postNotification(n);

                //Send confirmation if type requires it
                try {
                    n.sendConfirmation();
                } catch (IOException e) {
                    Utils.log(e);
                }

                //TODO
                //de.tum.in.tumcampusapp.managers.NotificationManager notificationManager
                //        = new de.tum.in.tumcampusapp.managers.NotificationManager(this);
                //notificationManager.replaceInto(n);
            }
        } else {

            //Try to match it as a legacy chat notification
            try {
                Bundle bundle = new Bundle();
                for (String key : data.keySet()) {
                    bundle.putString(key, data.get(key));
                }
                this.postNotification(new ChatNotification(bundle, this, -1));
            } catch (Exception e) {
                //@todo do something
            }
        }
    }

    /**
     * @param n the Notification to post
     */
    private void postNotification(GenericNotification n) {
        if (n != null) {
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification note = n.getNotification();
            if (note != null) {
                mNotificationManager.notify(n.getNotificationIdentification(), note);
            }
        }
    }

}