package de.tum.in.tumcampusapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Moh on 6/19/2016.
 */
public class FavoriteDishReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, FavoriteDishService.class);
        context.startService(i);
    }
}
