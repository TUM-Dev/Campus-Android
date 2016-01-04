package de.tum.in.tumcampus.notifications;

import android.app.Notification;
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
import de.tum.in.tumcampus.exceptions.NoPrivateKey;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.GCMChat;
import de.tum.in.tumcampus.models.TUMCabeClient;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.ChatMessageManager;

public class Chat extends GenericNotification {

    public static final int NOTIFICATION_ID = CardManager.CARD_CHAT;

    private final GCMChat extras;

    private ChatRoom chatRoom;
    private String notificationText;
    private TaskStackBuilder sBuilder;


    public Chat(Bundle extras, Context context, int notfication) {
        super(context, 1, notfication, true);

        //Initialize the object keeping important infos about the update
        this.extras = new GCMChat();

        //Get the update details
        this.extras.room = Integer.parseInt(extras.getString("room"));
        this.extras.member = Integer.parseInt(extras.getString("member"));

        //Message part is only present if we have a updated message
        if (extras.containsKey("message")) {
            this.extras.message = Integer.parseInt(extras.getString("message"));
        } else {
            this.extras.message = -1;
        }

        this.prepare();
    }

    public Chat(String payload, Context context, int notfication) {
        super(context, 1, notfication, true);

        //Check if a payload was passed
        if (payload == null) {
            throw new NullPointerException();
        }

        // parse data
        this.extras = (new Gson()).fromJson(payload, GCMChat.class);

        this.prepare();
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

    private void prepare() {
        Utils.logv("Received GCM notification: room=" + this.extras.room + " member=" + this.extras.member + " message=" + this.extras.message);

        // Get the data necessary for the ChatActivity
        ChatMember member = Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember.class);
        chatRoom = TUMCabeClient.getInstance(context).getChatRoom(this.extras.room);

        ChatMessageManager manager = new ChatMessageManager(context, chatRoom.getId());
        Cursor messages = null;
        try {
            messages = manager.getNewMessages(member, this.extras.message);
        } catch (NoPrivateKey noPrivateKey) {
            noPrivateKey.printStackTrace();
        }

        // Notify any open chat activity that a message has been received
        Intent intent = new Intent("chat-message-received");
        intent.putExtra("GCMChat", this.extras);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        notificationText = null;
        if (messages != null && messages.moveToFirst()) {
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

    public Notification getNotification() {
        //Check if chat is currently open then don't show a notification if it is
        if (ChatActivity.mCurrentOpenChatRoom != null && this.extras.room == ChatActivity.mCurrentOpenChatRoom.getId()) {
            return null;
        }

        if (Utils.getSettingBool(context, "card_chat_phone", true) && this.extras.message == -1) {

            PendingIntent contentIntent = sBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

            // GCMNotification sound
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
                    .setSmallIcon(this.icon)
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

    @Override
    public int getNotificationIdentification() {
        return this.extras.room << 4 + Chat.NOTIFICATION_ID;
    }
}
