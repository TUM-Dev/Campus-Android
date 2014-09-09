package de.tum.in.tumcampus.auxiliary;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;

/**
 * Counts the usage of a specific activity
 */
public class ImplicitCounter {

    /**
     * Counting number of the times that the user used this activity.
     * */
    public static void Counter(ActionBarActivity context) {
        SharedPreferences sp = context.getSharedPreferences("usage_counter", Context.MODE_PRIVATE);
        final String identifier = context.getClass().getSimpleName();

        final int currentUsages = sp.getInt(identifier, 0);
        sp.edit().putInt(identifier, currentUsages+1).apply();
    }
}
