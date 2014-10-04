package de.tum.in.tumcampus.auxiliary;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Counts the usage of a specific activity
 */
public class ImplicitCounter extends AsyncTask<String, Integer, Void> {
    private static final String settings = "usage_counter";
    private static final String URL = "https://tumcabe.in.tum.de/Api/statistics/";
    private static final String tag = "ImplicitCounter";

    private Context c = null;

    /**
     * Counting number of the times that the user used this activity.
     * Only call this in the {@link android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)}
     * method of a direct subclass of {@link android.support.v7.app.ActionBarActivity}
     *
     * @param c Pointer to the activity that has been opened
     */
    public static void Counter(Context c) {
        SharedPreferences sp = c.getSharedPreferences(settings, Context.MODE_PRIVATE);
        final String identifier = c.getClass().getSimpleName();

        final int currentUsages = sp.getInt(identifier, 0);
        sp.edit().putInt(identifier, currentUsages + 1).apply();
    }


    public void submitCounter(Context c) {
        if (c == null) {
            Log.e("Stats submit", "No context passed!");
            return;
        }
        this.c = c;
        SharedPreferences sp = this.c.getSharedPreferences(settings, Context.MODE_PRIVATE);

        // Get all current entries
        Map<String, ?> allEntries = sp.getAll();

        // Submit this to webservice via parent async class
        this.execute((new Gson().toJson(allEntries)).toString());

        // Delete / Reset
        SharedPreferences.Editor e = sp.edit();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            e.putInt(entry.getKey(), 0);
        }
        e.apply();
    }

    protected Void doInBackground(String... data) {
        if (data == null) {
            Log.e(tag, "No Json data passed, skipping...");
            return null;
        }

        // Transmit stack trace with PUT request
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpParams params = httpClient.getParams();
        HttpProtocolParams.setUseExpectContinue(params, false);

        HttpPut request = new HttpPut(URL);
        request.addHeader("X-DEVICE-ID", NetUtils.getDeviceID(this.c)); // Add our device identifier

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        //Add our payload which should be json encoded
        nvps.add(new BasicNameValuePair("data", data[0]));

        //Send the request
        try {
            request.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            // We don't care about the response, so we just hope it went well and on with it.
            httpClient.execute(request);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Return nothing :)
        return null;
    }

}
