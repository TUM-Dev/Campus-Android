package de.tum.in.tumcampus.notifications;

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
import de.tum.in.tumcampus.models.TUMCabeClient;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.ChatMessageManager;

public class ChatNotification extends Notification {

    public static final int NOTIFICATION_ID = CardManager.CARD_CHAT;
    public final int chatRoomId;
    private final int messageId;
    private final ChatRoom chatRoom;
    private String txt;
    private final TaskStackBuilder sBuilder;
    private final Context context;

    public ChatNotification(Bundle extras, Context context) {
        this.context = context;
        //Get the update details
        chatRoomId = Integer.parseInt(extras.getString("room"));
        int memberId = Integer.parseInt(extras.getString("member"));
        if (extras.containsKey("message")) {
            messageId = Integer.parseInt(extras.getString("message"));
        } else {
            messageId = -1;
        }

        Utils.logv("Received GCM notification: room=" + chatRoomId + " member=" + memberId + " message=" + messageId);

        // Get the data necessary for the ChatActivity
        ChatMember member = Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember.class);
        chatRoom = TUMCabeClient.getInstance(context).getChatRoom(chatRoomId);

        ChatMessageManager manager = new ChatMessageManager(context, chatRoom.getId());
        Cursor messages = manager.getNewMessages(getPrivateKeyFromSharedPrefs(context), member, messageId);

        // Notify any open chat activity that a message has been received
        Intent intent = new Intent("chat-message-received");
        intent.putExtras(extras);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

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
        Intent notificationIntent = new Intent(context, ChatActivity.class);
        notificationIntent.putExtra(Const.CURRENT_CHAT_ROOM, new Gson().toJson(chatRoom));

        sBuilder = TaskStackBuilder.create(context);
        sBuilder.addNextIntent(new Intent(context, MainActivity.class));
        sBuilder.addNextIntent(new Intent(context, ChatRoomsActivity.class));
        sBuilder.addNextIntent(notificationIntent);
    }

    public Notification getChatNotification() {
        //Check if chat is currently open then don't show a notification if it is
        if (ChatActivity.mCurrentOpenChatRoom != null && chatRoomId == ChatActivity.mCurrentOpenChatRoom.getId()) {
            return null;
        }

        if (Utils.getSettingBool(context, "card_chat_phone", true) && messageId == -1) {

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

            //Show a nice notification
            return new NotificationCompat.Builder(context)
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
