package de.tum.in.tumcampusapp.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;

import java.io.IOException;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.MainActivity;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.gcm.GCMNotification;
import de.tum.in.tumcampusapp.models.gcm.GCMUpdate;

public class Update extends GenericNotification {

    public final GCMUpdate data;
    private final GCMNotification info;

    public Update(String payload, Context context, int notification) {
        super(context, 2, notification, true);

        //Check if a payload was passed
        if (payload == null) {
            throw new IllegalArgumentException();
        }

        // parse data
        this.data = new Gson().fromJson(payload, GCMUpdate.class);

        //Get data from server
        this.info = getNotificationFromServer();

        //if (BuildConfig.VERSION_CODE < data.packageVersion) {
        //TODO self deactivate
        //}
    }

    private GCMNotification getNotificationFromServer() {
        try {
            return TUMCabeClient.getInstance(this.context)
                                .getNotification(this.notification);
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    @Override
    public Notification getNotification() {
        if (data.getSdkVersion() > Build.VERSION.SDK_INT || BuildConfig.VERSION_CODE >= data.getPackageVersion()) {
            return null;
        }

        // GCMNotification sound
        Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.message);
        Intent alarm = new Intent(this.context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this.context, 0, alarm, PendingIntent.FLAG_UPDATE_CURRENT);

        final String description;
        if (info.getDescription() == null || "".equals(info.getDescription())) {
            description = String.format(context.getString(R.string.update_notification_description), data.getReleaseDate());
        } else {
            description = info.getDescription();
        }

        final String title;
        if (info.getTitle() == null || "".equals(info.getTitle())) {
            title = context.getString(R.string.update);
        } else {
            title = info.getTitle();
        }

        return new NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setSmallIcon(this.icon)
                .setContentTitle(title)
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
