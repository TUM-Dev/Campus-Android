package de.tum.in.tumcampus.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.gson.Gson;

import de.tum.in.tumcampus.BuildConfig;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.MainActivity;
import de.tum.in.tumcampus.models.GCMUpdate;

public class Update extends GenericNotification {

    public final GCMUpdate data;

    private TaskStackBuilder sBuilder;

    public Update(String payload, Context context, int notfication) {
        super(context, 2, notfication, true);

        //Check if a payload was passed
        if (payload == null) {
            throw new NullPointerException();
        }

        // parse data
        this.data = (new Gson()).fromJson(payload, GCMUpdate.class);

        if (BuildConfig.VERSION_CODE < data.packageVersion) {
            //TODO self deactivate
        }
    }


    @Override
    public Notification getNotification() {
        if (data.sdkVersion > Build.VERSION.SDK_INT || BuildConfig.VERSION_CODE >= data.packageVersion) {
            return null;
        }

        // GCMNotification sound
        Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.message);
        Intent alarm = new Intent(this.context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this.context, 0, alarm, PendingIntent.FLAG_UPDATE_CURRENT);

        final String description = String.format(context.getString(R.string.update_notification_description), data.releaseDate);

        return new NotificationCompat.Builder(context)
                .setSmallIcon(this.icon)
                .setContentTitle(context.getString(R.string.update))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setContentText(description)
                .setContentIntent(pending)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setLights(0xff0000ff, 500, 500)
                .setSound(sound)
                .setAutoCancel(true)
                .build();
    }

    @Override
    public int getNotificationIdentification() {
        return 2;
    }
}
