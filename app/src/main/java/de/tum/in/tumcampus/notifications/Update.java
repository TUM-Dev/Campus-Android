package de.tum.in.tumcampus.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;

import com.google.gson.Gson;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.ChatActivity;
import de.tum.in.tumcampus.models.GCMNotification;
import de.tum.in.tumcampus.models.GCMUpdate;
import de.tum.in.tumcampus.models.TUMCabeClient;

public class Update extends GenericNotification {

    public final GCMUpdate data;
    private GCMNotification info;

    private TaskStackBuilder sBuilder;

    public Update(String payload, Context context, int notfication) {
        super(context, 2, notfication, true);

        //Check if a payload was passed
        if (payload == null) {
            throw new NullPointerException();
        }

        // parse data
        this.data = (new Gson()).fromJson(payload, GCMUpdate.class);

        //Get data from server
        this.info = TUMCabeClient.getInstance(this.context).getNotification(this.notification);
    }


    @Override
    public Notification getNotification() {
        //@todo
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


        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.tum_logo_notification)
                .setContentTitle("TCA Alarm")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(data.packageVersion + ""))
                .setContentText(data.packageVersion + "")
                .setContentIntent(contentIntent)
                .setDefaults(android.app.Notification.DEFAULT_VIBRATE)
                .setLights(0xff0000ff, 500, 500)
                .setSound(sound)
                .setAutoCancel(true)
                .extend(new NotificationCompat.WearableExtender().addAction(action))
                .build();
    }

    @Override
    public int getNotificationIdentification() {
        return 2;
    }
}
