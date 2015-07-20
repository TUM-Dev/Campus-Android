package de.tum.in.tumcampus.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.MainActivity;
import de.tum.in.tumcampus.models.GCMAlert;
import de.tum.in.tumcampus.models.GCMNotification;
import de.tum.in.tumcampus.models.TUMCabeClient;

public class Alarm extends GenericNotification {

    public final GCMAlert alert;
    private GCMNotification info;

    public Alarm(String payload, Context context, int notification) {
        super(context, 3, notification, true);

        //Check if a payload was passed
        if (payload == null) {
            throw new NullPointerException();
        }

        //Get data from server
        this.info = TUMCabeClient.getInstance(this.context).getNotification(this.notification);

        // parse data
        this.alert = (new Gson()).fromJson(payload, GCMAlert.class);
    }


    @Override
    public Notification getNotification() {
        if (alert.silent || info == null) {
            //Do nothing
            return null;
        }

        // GCMNotification sound
        Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.message);
        Intent alarm = new Intent(this.context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this.context, 0, alarm, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context)
                .setSmallIcon(this.icon)
                .setContentTitle(info.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(info.getDescription()))
                .setContentText(info.getDescription())
                .setContentIntent(pending)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setLights(0xff0000ff, 500, 500)
                .setSound(sound)
                .setAutoCancel(true)
                .build();
    }

    @Override
    public int getNotificationIdentification() {
        return 3;
    }
}
