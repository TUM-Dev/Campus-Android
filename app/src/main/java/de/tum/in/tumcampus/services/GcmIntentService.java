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

package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.tum.in.tumcampus.models.TUMCabeClient;
import de.tum.in.tumcampus.notifications.Alarm;
import de.tum.in.tumcampus.notifications.Chat;
import de.tum.in.tumcampus.notifications.GenericNotification;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        // The getMessageType() intent parameter must be the intent you received in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        //Check that we have some data and the intent was indeed a gcm message (gcm might be subject to change in the future)
        if (!extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {  // has effect of un-parcelling Bundle
            //Legacy messages need to be handled - maybe some data is missing?
            if (!extras.containsKey("payload") || !extras.containsKey("type")) {

                //Try to match it as a legacy chat notification
                try {
                    this.sendChatNotification(extras);
                } catch (Exception e) {
                    //@todo do something
                }
            } else {
                //Get some important values
                int notification = extras.getInt("notificaton");
                int type = extras.getInt("type");

                //Initialize our outputs
                GenericNotification n = null;

                //switch on the type as both the type and payload must be present
                switch (type) { //https://github.com/TCA-Team/TumCampusApp/wiki/GCM-Message-format
                    case 0: //Nothing to do, just confirm the retrieved notification
                        TUMCabeClient.getInstance(this).confirm(notification);
                        break;
                    case 1: //Chat
                        n = new Chat(extras.getString("payload"), this, notification);
                        break;
                    case 2: //Update
                        n = new Alarm(extras.getString("payload"), this, notification);
                        break;
                    case 3: //Alert
                        n = new Alarm(extras.getString("payload"), this, notification);
                        break;
                }

                //Post & save the notification if it was of any significance
                if (n != null) {
                    this.postNotification(n);

                    //Send confirmation if type requires it
                    n.sendConfirmation();

                    de.tum.in.tumcampus.models.managers.NotificationManager man = new de.tum.in.tumcampus.models.managers.NotificationManager(this);
                    //@todo save to our notificationmanager
                }
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Put the message into a notification and post it.
     *
     * @param extras
     */
    private void sendChatNotification(Bundle extras) {
        this.postNotification(new Chat(extras, this, -1));
    }

    /**
     *
     * @param n
     */
    private void postNotification(GenericNotification n) {
        if (n != null) {
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(n.getNotificationIdentification(), n.getNotification());
        }
    }


}