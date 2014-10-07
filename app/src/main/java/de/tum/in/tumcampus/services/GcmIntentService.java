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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.ChatActivity;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.services.GcmBroadcastReceiver;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    private static final int NOTIFICATION_ID = 1;
 
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
 
        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
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
    	String chatRoomString = extras.getString("chat_room"); // chat_room={"id":3}
    	Pattern pattern = Pattern.compile("\\{\"id\":(.*)\\}");
    	Matcher matcher = pattern.matcher(chatRoomString);
    	if (!matcher.find()) {
    		return;
    	}
    	String msg = extras.getString("text");
		String chatRoomId = matcher.group(1);
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Notify chat activity that a message has been received
        Intent intent = new Intent("chat-message-received");
		intent.putExtras(extras);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        
        Intent notificationIntent = new Intent(this, ChatActivity.class);
        
        // Get the data necessary for the ChatActivity
        String lrzId = PreferenceManager.getDefaultSharedPreferences(this).getString(Const.LRZ_ID, "");
        List<ChatMember> members = ChatClient.getInstance(this).getMember(lrzId);
        ChatRoom chatRoom = ChatClient.getInstance(this).getChatRoom(chatRoomId);
        // Put the data into the intent
        notificationIntent.putExtra(Const.CURRENT_CHAT_ROOM, new Gson().toJson(chatRoom));
        notificationIntent.putExtra(Const.CURRENT_CHAT_MEMBER, new Gson().toJson(members.get(0)));
        
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_ONE_SHOT);
         
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.tum_logo_notification)
        .setContentTitle("TCA Chat")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg)
        .setContentIntent(contentIntent)
        .setDefaults(Notification.DEFAULT_ALL)
        .setAutoCancel(true);
        
        Notification notification = mBuilder.build();
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }
}