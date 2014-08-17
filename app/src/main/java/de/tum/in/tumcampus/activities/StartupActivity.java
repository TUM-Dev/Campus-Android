package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.DemoModeStartActivity;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
 * Entrance point of the App.
 *
 * @author Sascha Moecker
 */
public class StartupActivity extends ActionBarActivity {
    public static final boolean DEMO_MODE = false;
    public static final boolean TRACK_ERRORS_WITH_BUG_SENSE = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Show a loading screen during boot
        this.setContentView(R.layout.activity_startup);

        new BootTask().execute();

    }

    private class BootTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            CardManager.update(StartupActivity.this);

            // Init a Bug Report to https://www.bugsense.com
            if (TRACK_ERRORS_WITH_BUG_SENSE) {
                Log.d(this.getClass().getSimpleName(), "BugSense initialized");
                BugSenseHandler.initAndStartSession(StartupActivity.this, "19d18764");
            }

            // Workaround for new API version. There was a security update which
            // disallows applications to execute HTTP request in the GUI main
            // thread.
            if (android.os.Build.VERSION.SDK_INT > 8) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            // Start the demo Activity if demo mode is set
            if (DEMO_MODE) {
                Intent intent = new Intent(StartupActivity.this, DemoModeStartActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(StartupActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
            }

            //PreferenceManager.getDefaultSharedPreferences(this).edit().remove(Const.CHAT_ROOM_DISPLAY_NAME).commit();
            //PreferenceManager.getDefaultSharedPreferences(this).edit().remove(Const.PRIVATE_KEY).commit();
            return null;
        }

    }

}
