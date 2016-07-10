package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.CafeteriaActivity;
import de.tum.in.tumcampusapp.activities.MainActivity;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.Cafeteria;
import de.tum.in.tumcampusapp.models.managers.CafeteriaManager;
import de.tum.in.tumcampusapp.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampusapp.models.managers.SurveyManager;
import de.tum.in.tumcampusapp.trace.Util;

public class FavoriteDishService extends IntentService {

    private static final String FAVORITEDISH_SERVICE = "FavoriteDish";
    private CafeteriaMenuManager cafeteriaMenuManager;

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
        cafeteriaMenuManager = new CafeteriaMenuManager(this);
        Cursor c = cafeteriaMenuManager.getFavoriteDishToday();
        int index = 0;
        if (c.getCount() > 0) {
            c.moveToFirst();
            do {
                intent = new Intent(this, CafeteriaActivity.class);
                intent.putExtra(Const.MENSA_FOR_FAVORITEDISH, c.getInt(1));
                PendingIntent pi = PendingIntent.getActivity(this, index, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Your Favorite Dish!")
                        .setContentText(c.getString(0))
                        .setAutoCancel(true);

                mBuilder.setContentIntent(pi);
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                mBuilder.setAutoCancel(true);
                NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(index, mBuilder.build());
                index++;
            }
            while (c.moveToNext());
        }
    }
}
