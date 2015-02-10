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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.ChatActivity;
import de.tum.in.tumcampus.activities.ChatRoomsActivity;
import de.tum.in.tumcampus.activities.MainActivity;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.ChatMessageManager;

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
    private void sendNotification(Bundle extras) {
        //Get the update details
        int chatRoomId = Integer.parseInt(extras.getString("room"));
        int memberId = Integer.parseInt(extras.getString("member"));
        int messageId = -1;
        if (extras.containsKey("message")) {
            messageId = Integer.parseInt(extras.getString("message"));
        }

        Utils.logv("Received GCM notification: room=" + chatRoomId + " member=" + memberId + " message=" + messageId);

        // Get the data necessary for the ChatActivity
        ChatMember member = Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class);
        ChatRoom chatRoom = ChatClient.getInstance(this).getChatRoom(chatRoomId);

        ChatMessageManager manager = new ChatMessageManager(this, chatRoom.getId());
        Cursor messages = manager.getNewMessages(this.getPrivateKeyFromSharedPrefs(), member, messageId);

        // Notify any open chat activity that a message has been received
        Intent intent = new Intent("chat-message-received");
        intent.putExtras(extras);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        //Check if chat is currently open then don't show a notification if it is
        if (ChatActivity.mCurrentOpenChatRoom != null && chatRoomId == ChatActivity.mCurrentOpenChatRoom.getId()) {
            return;
        }

        String txt = null;
        if (messages.moveToFirst()) {
            do {
                if (txt == null)
                    txt = messages.getString(3);
                else
                    txt += "\n" + messages.getString(3);
            } while (messages.moveToNext());
        }

        // Put the data into the intent
        Intent notificationIntent = new Intent(this, ChatActivity.class);
        notificationIntent.putExtra(Const.CURRENT_CHAT_ROOM, new Gson().toJson(chatRoom));

        TaskStackBuilder sBuilder = TaskStackBuilder.create(this);
        sBuilder.addNextIntent(new Intent(this, MainActivity.class));
        sBuilder.addNextIntent(new Intent(this, ChatRoomsActivity.class));
        sBuilder.addNextIntent(notificationIntent);

        if (Utils.getSettingBool(this, "card_chat_phone", true) && messageId == -1) {

            PendingIntent contentIntent = sBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT );

            // Notification sound
            Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.message);


            String replyLabel = getResources().getString(R.string.reply_label);

            RemoteInput remoteInput = new RemoteInput.Builder(ChatActivity.EXTRA_VOICE_REPLY)
                    .setLabel(replyLabel)
                    .build();

            // Create the reply action and add the remote input
            NotificationCompat.Action action =
                    new NotificationCompat.Action.Builder(R.drawable.ic_reply,
                            getString(R.string.reply_label), contentIntent)
                            .addRemoteInput(remoteInput)
                            .build();

            //Show a nice notification
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.tum_logo_notification)
                    .setContentTitle(chatRoom.getName().substring(4))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(txt))
                    .setContentText(txt)
                    .setContentIntent(contentIntent)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setLights(0xff0000ff, 500, 500)
                    .setSound(sound)
                    .setAutoCancel(true)
                    .extend(new NotificationCompat.WearableExtender().addAction(action))
                    .build();

            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(chatRoomId << 4 + NOTIFICATION_ID, notification);
        }
    }

    /**
     * Loads the private key from preferences
     *
     * @return The private key object
     */
    private PrivateKey getPrivateKeyFromSharedPrefs() {
        String privateKeyString = Utils.getInternalSettingString(this, Const.PRIVATE_KEY, "");
        byte[] privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException e) {
            Utils.log(e);
        } catch (InvalidKeySpecException e) {
            Utils.log(e);
        }
        return null;
    }
}