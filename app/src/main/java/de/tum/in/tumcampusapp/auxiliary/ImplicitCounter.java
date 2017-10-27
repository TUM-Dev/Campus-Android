package de.tum.in.tumcampusapp.auxiliary;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Date;
import java.util.Map;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.cards.NewsCard;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.models.tumcabe.Statistics;

/**
 * Counts the usage of a specific activity
 */
public final class ImplicitCounter {
    private static final String USAGE_COUNTER = "usage_counter";
    private static Date lastSync;

    private ImplicitCounter() {
        // ImplicitCounter is a utility class
    }

    /**
     * Counting number of the times that the user used this activity.
     * Only call this in the {@link android.support.v7.app.AppCompatActivity#onCreate(android.os.Bundle)}
     * method of a direct subclass of {@link android.support.v7.app.AppCompatActivity}
     *
     * @param c Pointer to the activity that has been opened
     */
    public static void count(Activity c) {
        SharedPreferences sp = c.getSharedPreferences(USAGE_COUNTER, Context.MODE_PRIVATE);
        final String identifier = c.getClass()
                                   .getSimpleName();

        final int currentUsages = sp.getInt(identifier, 0);
        sp.edit()
          .putInt(identifier, currentUsages + 1)
          .apply();
    }

    public static void countCard(Context c, Card card) {
        SharedPreferences sp = c.getSharedPreferences(USAGE_COUNTER, Context.MODE_PRIVATE);
        StringBuilder identifier = new StringBuilder(card.getClass()
                                                         .getSimpleName());

        //Add the news id when showing a news card so we can check which feeds are used
        if (card instanceof NewsCard) {
            identifier.append(((NewsCard) card).getSource());
        }

        final int currentUsages = sp.getInt(identifier.toString(), 0);
        sp.edit()
          .putInt(identifier.toString(), currentUsages + 1)
          .apply();
    }

    public static void submitCounter(Context c) {
        //Check first: sync only every so often - in this case one hour
        Date interval = new Date();
        interval.setTime(interval.getTime() - 1000 * 3600);
        if (lastSync != null && lastSync.after(interval)) {
            return;
        }
        lastSync = new Date();

        //Check if context passed
        if (c == null) {
            Utils.log("Stats submit: No context passed!");
            return;
        }
        //Get the prefs
        SharedPreferences sp = c.getSharedPreferences(USAGE_COUNTER, Context.MODE_PRIVATE);

        // Get all current entries
        Map<String, ?> allEntries = sp.getAll();

        // Submit this to webservice via parent async class
        TUMCabeClient.getInstance(c)
                     .putStatistics(new Statistics(new Gson().toJson(allEntries)));

        // Delete / Reset
        SharedPreferences.Editor e = sp.edit();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            e.remove(entry.getKey());
        }
        e.apply();
    }

}
