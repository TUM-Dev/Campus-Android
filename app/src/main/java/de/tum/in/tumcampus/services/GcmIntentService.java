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

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.ChatActivity;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    private static final int NOTIFICATION_ID = CardManager.CARD_CHAT;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of un-parcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                sendNotification(extras);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you
    // might choose to do with a GCM message.
    private void sendNotification(Bundle extras) {
        //Get the update details
        String chatRoomId = extras.getString("room"); // chat_room={"id":3}
        //String msg = extras.getString("text");

        // Notify any open chat activity that a message has been received
        Intent intent = new Intent("chat-message-received");
        intent.putExtras(extras);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // Get the data necessary for the ChatActivity
        String lrzId = Utils.getSetting(this, Const.LRZ_ID, "");
        List<ChatMember> members = ChatClient.getInstance(this).getMember(lrzId);
        ChatRoom chatRoom = ChatClient.getInstance(this).getChatRoom(chatRoomId);

        //Check if chat is currently open then don't show a notification if it is
        if (this.isChatOpen()) {
            return;
        }

        // Put the data into the intent
        Intent notificationIntent = new Intent(this, ChatActivity.class);
        notificationIntent.putExtra(Const.CURRENT_CHAT_ROOM, new Gson().toJson(chatRoom));
        notificationIntent.putExtra(Const.CURRENT_CHAT_MEMBER, new Gson().toJson(members.get(0)));

        if(Utils.getSettingBool(this, "card_chat_phone", true)) {

            // Don't show notification if chat room is open
            if(ChatActivity.mCurrentlyOpenChatRoom!=null && ChatActivity.mCurrentlyOpenChatRoom.getGroupId().equals(chatRoomId))
                return;

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

            //Show a nice notification
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.tum_logo_notification)
                            .setContentTitle("TCA Chat")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("New message arrived"))
                        .setContentText("New message arrived")
                            .setContentIntent(contentIntent)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setAutoCancel(true);

            Notification notification = mBuilder.build();
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Boolean isChatOpen() {
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> allTasks = am.getRunningTasks(1);

        for (ActivityManager.RunningTaskInfo aTask : allTasks) {
            if (aTask.topActivity.getClassName().equals("de.tum.in.tumcampus.activities.ChatActivity")) {
                return true;
            }
        }
        return false;
    }
}