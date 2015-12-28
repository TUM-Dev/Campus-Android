package de.tum.in.tumcampus.auxiliary;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Pair;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.NewsCard;

/**
 * Counts the usage of a specific activity
 */
public class ImplicitCounter extends AsyncTask<String, Integer, Void> {
    private static final String settings = "usage_counter";
    private static final String TUMCABE_URL = "https://tumcabe.in.tum.de/Api/statistics/";
    private static Date lastSync = null;

    private Context c = null;

    /**
     * Counting number of the times that the user used this activity.
     * Only call this in the {@link android.support.v7.app.AppCompatActivity#onCreate(android.os.Bundle)}
     * method of a direct subclass of {@link android.support.v7.app.AppCompatActivity}
     *
     * @param c Pointer to the activity that has been opened
     */
    public static void Counter(Context c) {
        SharedPreferences sp = c.getSharedPreferences(settings, Context.MODE_PRIVATE);
        final String identifier = c.getClass().getSimpleName();

        final int currentUsages = sp.getInt(identifier, 0);
        sp.edit().putInt(identifier, currentUsages + 1).apply();
    }

    public static void CounterCard(Context c, Card card) {
        SharedPreferences sp = c.getSharedPreferences(settings, Context.MODE_PRIVATE);
        String identifier = card.getClass().getSimpleName();

        //Add the news id when showing a news card so we can check which feeds are used
        if (card instanceof NewsCard) {
            identifier += ((NewsCard) card).getSource();
        }

        final int currentUsages = sp.getInt(identifier, 0);
        sp.edit().putInt(identifier, currentUsages + 1).apply();
    }


    public void submitCounter(Context c) {
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
        this.c = c;

        //Get the prefs
        SharedPreferences sp = this.c.getSharedPreferences(settings, Context.MODE_PRIVATE);

        // Get all current entries
        Map<String, ?> allEntries = sp.getAll();

        // Submit this to webservice via parent async class
        this.execute(new Gson().toJson(allEntries));

        // Delete / Reset
        SharedPreferences.Editor e = sp.edit();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            e.remove(entry.getKey());
        }
        e.apply();
    }

    protected Void doInBackground(String... data) {
        if (data == null) {
            Utils.log("No Json data passed, skipping...");
            return null;
        }

        // Transmit stack trace with PUT request
        HttpURLConnection request = null;
        try {
            request = (HttpURLConnection) (new URL(TUMCABE_URL)).openConnection();
            request.setRequestMethod("PUT");
            request.setDoOutput(true);
            request.addRequestProperty("X-DEVICE-ID", NetUtils.getDeviceID(this.c)); // Add our device identifier

            List<Pair<String, String>> nvps = Collections.singletonList(
                    //Add our payload which should be json encoded
                    new Pair<>("data", data[0])
            );

            //Send the request
            OutputStream outputStream = request.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(NetUtils.buildParamString(nvps));
            writer.flush();
            writer.close();
            outputStream.close();
            // We don't care about the response, so we just hope it went well and on with it.
        } catch (IOException e) {
            Utils.log(e);
        } finally {
            if (request != null) {
                request.disconnect();
            }
        }

        //Return nothing :)
        return null;
    }

}
