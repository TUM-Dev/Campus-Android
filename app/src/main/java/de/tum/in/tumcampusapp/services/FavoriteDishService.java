package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.MainActivity;

/**
 * Created by Moh on 6/19/2016.
 */
public class FavoriteDishService extends IntentService {

    private static final String FAVORITEDISH_SERVICE = "FavoriteDish";

    public FavoriteDishService() {
        super(FAVORITEDISH_SERVICE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        /**
         * create a notification that dish is available.
         */
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 1, notificationIntent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("TUM-MENSA")
                .setContentText("your favorite dish is available today")
                .setAutoCancel(true);

        mBuilder.setContentIntent(pi);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}