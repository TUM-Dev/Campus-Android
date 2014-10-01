package de.tum.in.tumcampus.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.io.File;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.wizard.WizNavStartActivity;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.FileUtils;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.DatabaseManager;
import de.tum.in.tumcampus.services.DownloadService;
import de.tum.in.tumcampus.services.StartSyncReceiver;

/**
 * Entrance point of the App.
 */
public class StartupActivity extends ActionBarActivity {
    private static final boolean TRACK_ERRORS_WITH_BUG_SENSE = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);

        //Show a loading screen during boot
        setContentView(R.layout.activity_startup);

        // Init a Bug Report to https://www.bugsense.com
        if (TRACK_ERRORS_WITH_BUG_SENSE) {
            Utils.log("BugSense initialized");
            BugSenseHandler.initAndStartSession(this, "19d18764");
        }

        // For compatibility reasons
        int prevVersion = Utils.getInternalSettingInt(this, Const.APP_VERSION, 35);

        // get current app version
        int currentVersion = 0;
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Utils.log(e);
        }
        if(prevVersion<currentVersion) {
            setupNewVersion();
            findViewById(R.id.startup_loading_first).setVisibility(View.VISIBLE);
            Utils.setInternalSetting(this, Const.APP_VERSION, currentVersion);
        }

        // Also First run setup of id and token
        // Check the flag if user wants the wizard to open at startup
        Boolean hideWizardOnStartup = Utils.getSettingBool(this, Const.HIDE_WIZARD_ON_STARTUP, false);
        if (!hideWizardOnStartup) {
            startActivity(new Intent(this, WizNavStartActivity.class));
            finish();
            return;
        }

        // Register receiver for background service
        IntentFilter filter = new IntentFilter(DownloadService.BROADCAST_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        // Start background service and ensure cards are set
        Intent i = new Intent(this, StartSyncReceiver.class);
        i.putExtra(Const.APP_LAUNCHES,true);
        sendBroadcast(i);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    /**
     * Broadcast receiver gets notified if {@link de.tum.in.tumcampus.services.BackgroundService}
     * has prepared cards to be displayed
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadService.BROADCAST_NAME)) {
                startApp();
            }
        }
    };

    /**
     * Animates the TUM logo into place (left upper corner) and animates background up.
     * Afterwards {@link MainActivity} gets started
     */
    private void startApp() {
        // Get views to be moved
        final View background = findViewById(R.id.startup_background);
        final ImageView tumLogo = (ImageView) findViewById(R.id.startup_tum_logo);
        final TextView loadingText = (TextView) findViewById(R.id.startup_loading);
        final ImageView drawerIndicator = (ImageView) findViewById(R.id.startup_drawer_indicator);
        final TextView actionBarTitle = (TextView) findViewById(R.id.startup_actionbar_title);
        final ImageView settings = (ImageView) findViewById(R.id.startup_settings);

        // Make some position calculations
        float density = getResources().getDisplayMetrics().density;
        final int actionBarHeight = getActionBarHeight();
        final float tumScale = (actionBarHeight-density*16)/(float)tumLogo.getHeight();
        final float screenHeight = background.getHeight();
        float moveToLeft = -ViewHelper.getX(tumLogo)-(tumLogo.getWidth()*(1-tumScale))/2.0f+8*density;
        float moveToTop = -ViewHelper.getY(tumLogo)-(tumLogo.getHeight()*(1-tumScale))/2.0f+8*density;

        // Setup animation
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(background, "translationY", ViewHelper.getTranslationX(background), actionBarHeight-screenHeight),
                ObjectAnimator.ofFloat(tumLogo, "translationX", 0, moveToLeft, moveToLeft),
                ObjectAnimator.ofFloat(tumLogo, "translationY", 0, moveToTop, moveToTop),
                ObjectAnimator.ofFloat(tumLogo, "scaleX", 1, tumScale, tumScale),
                ObjectAnimator.ofFloat(tumLogo, "scaleY", 1, tumScale, tumScale),
                ObjectAnimator.ofFloat(loadingText, "alpha", 1, 0, 0, 0),
                ObjectAnimator.ofFloat(loadingText, "translationY", 0, -screenHeight),
                ObjectAnimator.ofFloat(drawerIndicator, "translationX", -50*density, 0),
                ObjectAnimator.ofFloat(actionBarTitle, "alpha", 0, 0, 1),
                ObjectAnimator.ofFloat(settings, "alpha", 0, 0, 1)
        );
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                drawerIndicator.setVisibility(View.VISIBLE);
                actionBarTitle.setVisibility(View.VISIBLE);
                settings.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Start the demo Activity if demo mode is set
                Intent intent = new Intent(StartupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                //overridePendingTransition(0,0);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        set.setDuration(600).start();
    }

    /**
     * Gets the height of the actionbar
     * @return Actionbar height
     */
    private int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data, getResources().getDisplayMetrics());
        } else {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                    getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    /**
     * Delete stuff from old version
     */
    private void setupNewVersion() {
        SQLiteDatabase db = DatabaseManager.getDb(this);
        // reset sync manager
        db.execSQL("DROP TABLE IF EXISTS syncs");

        // drop cafeterias table
        db.execSQL("DROP TABLE IF EXISTS cafeterias");

        // drop transportation table
        db.execSQL("DROP TABLE IF EXISTS transports");

        // drop locations table
        db.execSQL("DROP TABLE IF EXISTS locations");

        // drop news table
        db.execSQL("DROP TABLE IF EXISTS news");

        // rename hide_wizzard_on_startup
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor e = sp.edit();
        e.putBoolean(Const.HIDE_WIZARD_ON_STARTUP, sp.getBoolean("hide_wizzard_on_startup", false));
        e.remove("hide_wizzard_on_startup");
        e.apply();

        // delete tumcampus directory
        File f = new File(Environment.getExternalStorageDirectory().getPath() + "/tumcampus");
        FileUtils.deleteRecursive(f);
    }
}
