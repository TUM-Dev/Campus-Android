package de.tum.in.tumcampusapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Intent receiver for the dish alarm
 */
public class FavoriteDishReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, FavoriteDishService.class);
        context.startService(i);
    }
}
