package de.tum.in.tumcampus.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.util.Base64;

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
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.GCMChat;
import de.tum.in.tumcampus.models.TUMCabeClient;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.ChatMessageManager;

public class ChatNotification extends Notification {

    public static final int NOTIFICATION_ID = CardManager.CARD_CHAT;

    public final int room;
    private final int message;
    private final int member;

    private ChatRoom chatRoom;
    private String notificationText;
    private TaskStackBuilder sBuilder;
    private final Context context;


    public ChatNotification(Bundle extras, Context context) {
        this.context = context;

        //Get the update details
        this.room = Integer.parseInt(extras.getString("room"));
        this.member = Integer.parseInt(extras.getString("member"));

        //Message part is only present if we have a updated message
        if (extras.containsKey("message")) {
            this.message = Integer.parseInt(extras.getString("message"));
        } else {
            this.message = -1;
        }

        this.prepare();
    }

    public ChatNotification(String payload, Context context) {
        this.context = context;

        //Check if a payload was passed
        if (payload == null) {
            throw new NullPointerException();
        }

        // parse data
        GCMChat extras = (new Gson()).fromJson(payload, GCMChat.class);

        //Get the update details
        this.room = extras.room;
        this.member = extras.member;
        this.message = extras.message;

        this.prepare();
    }

    private void prepare() {
        Utils.logv("Received GCM notification: room=" + room + " member=" + member + " message=" + message);

        // Get the data necessary for the ChatActivity
        ChatMember member = Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember.class);
        chatRoom = TUMCabeClient.getInstance(context).getChatRoom(room);

        ChatMessageManager manager = new ChatMessageManager(context, chatRoom.getId());
        Cursor messages = manager.getNewMessages(getPrivateKeyFromSharedPrefs(context), member, message);

        // Notify any open chat activity that a message has been received
        //@todo fix broadcast as extras might not be avaible
        //Intent intent = new Intent("chat-message-received");
        //intent.putExtras(extras);
        //LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        notificationText = null;
        if (messages.moveToFirst()) {
            do {
                if (notificationText == null)
                    notificationText = messages.getString(3);
                else
                    notificationText += "\n" + messages.getString(3);
            } while (messages.moveToNext());
        }

        // Put the data into the intent
        Intent notificationIntent = new Intent(context, ChatActivity.class);
        notificationIntent.putExtra(Const.CURRENT_CHAT_ROOM, new Gson().toJson(chatRoom));

        sBuilder = TaskStackBuilder.create(context);
        sBuilder.addNextIntent(new Intent(context, MainActivity.class));
        sBuilder.addNextIntent(new Intent(context, ChatRoomsActivity.class));
        sBuilder.addNextIntent(notificationIntent);
    }

    public android.app.Notification getNotification() {
        //Check if chat is currently open then don't show a notification if it is
        if (ChatActivity.mCurrentOpenChatRoom != null && room == ChatActivity.mCurrentOpenChatRoom.getId()) {
            return null;
        }

        if (Utils.getSettingBool(context, "card_chat_phone", true) && message == -1) {

            PendingIntent contentIntent = sBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

            // Notification sound
            Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.message);

            String replyLabel = context.getResources().getString(R.string.reply_label);

            RemoteInput remoteInput = new RemoteInput.Builder(ChatActivity.EXTRA_VOICE_REPLY)
                    .setLabel(replyLabel)
                    .build();

            // Create the reply action and add the remote input
            NotificationCompat.Action action =
                    new NotificationCompat.Action.Builder(R.drawable.ic_reply,
                            context.getString(R.string.reply_label), contentIntent)
                            .addRemoteInput(remoteInput)
                            .build();

            //Create a nice notification
            return new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.tum_logo_notification)
                    .setContentTitle(chatRoom.getName().substring(4))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                    .setContentText(notificationText)
                    .setContentIntent(contentIntent)
                    .setDefaults(android.app.Notification.DEFAULT_VIBRATE)
                    .setLights(0xff0000ff, 500, 500)
                    .setSound(sound)
                    .setAutoCancel(true)
                    .extend(new NotificationCompat.WearableExtender().addAction(action))
                    .build();

        }
        return null;
    }

    /**
     * Loads the private key from preferences
     *
     * @return The private key object
     */
    private static PrivateKey getPrivateKeyFromSharedPrefs(Context context) {
        String privateKeyString = Utils.getInternalSettingString(context, Const.PRIVATE_KEY, "");
        byte[] privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Utils.log(e);
        }
        return null;
    }
}
