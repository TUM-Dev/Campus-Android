package de.tum.in.tumcampus.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.wizzard.WizNavStartActivity;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.DemoModeStartActivity;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.services.DownloadService;
import de.tum.in.tumcampus.services.StartSyncReceiver;

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
        ImplicitCounter.Counter(this);

        //Show a loading screen during boot
        this.setContentView(R.layout.activity_startup);

        // Init a Bug Report to https://www.bugsense.com
        if (TRACK_ERRORS_WITH_BUG_SENSE) {
            Log.d(this.getClass().getSimpleName(), "BugSense initialized");
            BugSenseHandler.initAndStartSession(StartupActivity.this, "19d18764");
        }

        // Also First run setup of id and token
        // Check the flag if user wants the wizard to open at startup
        Boolean hideWizzardOnStartup = PreferenceManager.getDefaultSharedPreferences(StartupActivity.this).getBoolean(Const.HIDE_WIZZARD_ON_STARTUP, false);
        if (!hideWizzardOnStartup) {
            Intent intent = new Intent(StartupActivity.this, WizNavStartActivity.class);
            startActivity(intent);
            finish();
        }

        // Register receiver for background service
        IntentFilter filter = new IntentFilter(DownloadService.BROADCAST_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        // Start background service and ensure cards are set
        Intent i = new Intent(StartupActivity.this, StartSyncReceiver.class);
        i.putExtra(Const.APP_LAUNCHES,true);
        sendBroadcast(i);

        //PreferenceManager.getDefaultSharedPreferences(this).edit().remove(Const.CHAT_ROOM_DISPLAY_NAME).commit();
        //PreferenceManager.getDefaultSharedPreferences(this).edit().remove(Const.PRIVATE_KEY).commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadService.BROADCAST_NAME)) {
                startApp();
            }
        }
    };

    private void startApp() {
        // Start the demo Activity if demo mode is set
        if (DEMO_MODE) {
            Intent intent = new Intent(this, DemoModeStartActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
