package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import java.util.ArrayList;
import java.util.HashMap;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.CafeteriaActivity;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.managers.CafeteriaManager;

public class FavoriteDishService extends IntentService {

    private static final String FAVORITEDISH_SERVICE = "FavoriteDish";

    public FavoriteDishService() {
        super(FAVORITEDISH_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent handledCafeterias) {
        /**
         * create a notification that dish is available.
         */
        int bestMensa = handledCafeterias.getIntExtra("bestMensa",-1);
        CafeteriaManager cmm = new CafeteriaManager(getBaseContext());
        String bestMensaName = cmm.getMensaNameFromId(bestMensa);

        ArrayList<Integer> mensaIds = handledCafeterias.getIntegerArrayListExtra("mensaIds");
        HashMap<Integer,ArrayList<String>> favoriteDishes = new HashMap<>();
        for (int id : mensaIds){
            favoriteDishes.put(id, handledCafeterias.getStringArrayListExtra(""+id));
        }

        ArrayList<String> bestMensasDishes = favoriteDishes.get(bestMensa);

        for (int i=0; i < bestMensasDishes.size(); i++){
            Intent intent = new Intent(this, CafeteriaActivity.class);
            intent.putExtra(Const.MENSA_FOR_FAVORITEDISH, bestMensa);
            PendingIntent pi = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Food: "+bestMensaName)
                    .setContentText(bestMensasDishes.get(i))
                    .setAutoCancel(true);

            mBuilder.setContentIntent(pi);
            mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            mBuilder.setAutoCancel(true);
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(i, mBuilder.build());
        }
    }
}
