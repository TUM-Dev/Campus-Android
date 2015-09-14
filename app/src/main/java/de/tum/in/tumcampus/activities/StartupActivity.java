package de.tum.in.tumcampus.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.wizard.WizNavChatActivity;
import de.tum.in.tumcampus.activities.wizard.WizNavExtrasActivity;
import de.tum.in.tumcampus.activities.wizard.WizNavStartActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.FileUtils;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.DatabaseManager;
import de.tum.in.tumcampus.services.DownloadService;
import de.tum.in.tumcampus.services.StartSyncReceiver;
import de.tum.in.tumcampus.trace.ExceptionHandler;

/**
 * Entrance point of the App.
 */
public class StartupActivity extends AppCompatActivity {

    AtomicBoolean initializationFinished = new AtomicBoolean(false);
    private static final int REQUEST_LOCATION = 0;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Show a loading screen during boot
        setContentView(R.layout.activity_startup);

        //Our own Custom exception handler
        ExceptionHandler.setup(this);

        //Upload stats
        ImplicitCounter.Counter(this);
        (new ImplicitCounter()).submitCounter(this);

        // For compatibility reasons: big update happened with version 35
        int prevVersion = Utils.getInternalSettingInt(this, Const.APP_VERSION, 35);

        // get current app version
        int currentVersion = Utils.getAppVersion(this);
        boolean newVersion = prevVersion < currentVersion;
        if (newVersion) {
            this.setupNewVersion();
            Utils.setInternalSetting(this, Const.APP_VERSION, currentVersion);
        }

        // Also First run wizard for setup of id and token
        // Check the flag if user wants the wizard to open at startup
        boolean hideWizardOnStartup = Utils.getSettingBool(this, Const.HIDE_WIZARD_ON_STARTUP, false);
        String lrzId = Utils.getSetting(this, Const.LRZ_ID, ""); // If new version and LRZ ID is empty, start the full wizard
        if (!hideWizardOnStartup || (newVersion && lrzId.isEmpty())) {
            startActivity(new Intent(this, WizNavStartActivity.class));
            finish();
            return;
        } else if (newVersion) {
            Utils.setSetting(this, Const.BACKGROUND_MODE, true);
            Utils.setSetting(this, CardManager.SHOW_SUPPORT, true);

            Intent intent;
            if (new AccessTokenManager(this).hasValidAccessToken()) {
                intent = new Intent(this, WizNavChatActivity.class);
            } else {
                intent = new Intent(this, WizNavExtrasActivity.class);
            }
            intent.putExtra(Const.TOKEN_IS_SETUP, true);

            startActivity(intent);
            finish();
            return;
        }

        // On first setup show remark that loading could last longer than normally
        boolean isSetup = Utils.getInternalSettingBool(this, Const.EVERYTHING_SETUP, false);
        if (!isSetup) {
            findViewById(R.id.startup_loading_first).setVisibility(View.VISIBLE);
        }

        // Register receiver for background service
        IntentFilter filter = new IntentFilter(DownloadService.BROADCAST_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        // Start background service and ensure cards are set
        Intent i = new Intent(this, StartSyncReceiver.class);
        i.putExtra(Const.APP_LAUNCHES, true);
        sendBroadcast(i);

        //Request Permissions for Android 6.0
        requestLocationPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

                //Only proceed to start the App, if initialization is finished
                if (initializationFinished.compareAndSet(false, true)) {
                    return;
                }
                startApp();
            }
        }
    };

    /**
     * Request the Location Permission
     */
    private void requestLocationPermission() {
        //Check, if we already have permission
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Display an AlertDialog with an explanation and a button to trigger the request.
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.permission_location_explanation))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat
                                        .requestPermissions(StartupActivity.this, PERMISSIONS_LOCATION,
                                                REQUEST_LOCATION);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(StartupActivity.this, PERMISSIONS_LOCATION,
                        REQUEST_LOCATION);
            }
        } else {
            //We already got the permissions, to proceed normally
            //Only proceed to start the App, if initialization is finished
            if (initializationFinished.compareAndSet(false, true)) {
                return;
            }
            startApp();
        }
    }

    /**
     * Callback when the user allowed or denied Permissions
     * We do not care, if we got the permission or not, since the LocationManager needs to handle
     * missing permissions anyway
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Only proceed to start the App, if initialization is finished
        if (initializationFinished.compareAndSet(false, true)) {
            return;
        }
        startApp();
    }

    /**
     * Animates the TUM logo into place (left upper corner) and animates background up.
     * Afterwards {@link MainActivity} gets started
     */

    private void startApp() {
        // Get views to be moved
        final View background = findViewById(R.id.startup_background);
        final ImageView tumLogo = (ImageView) findViewById(R.id.startup_tum_logo);
        final TextView loadingText = (TextView) findViewById(R.id.startup_loading);
        final TextView first = (TextView) findViewById(R.id.startup_loading_first);

        // Make some position calculations
        final int actionBarHeight = getActionBarHeight();
        final float screenHeight = background.getHeight();

        // Setup animation
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(background, "translationY", ViewHelper.getTranslationX(background), actionBarHeight - screenHeight),
                ObjectAnimator.ofFloat(tumLogo, "alpha", 1, 0, 0),
                ObjectAnimator.ofFloat(loadingText, "alpha", 1, 0, 0),
                ObjectAnimator.ofFloat(first, "alpha", 1, 0, 0),
                ObjectAnimator.ofFloat(tumLogo, "translationY", 0, -screenHeight / 3),
                ObjectAnimator.ofFloat(loadingText, "translationY", 0, -screenHeight / 3),
                ObjectAnimator.ofFloat(first, "translationY", 0, -screenHeight / 3)
        );
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Start the demo Activity if demo mode is set
                Intent intent = new Intent(StartupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        set.setDuration(600).start();
    }

    /**
     * Gets the height of the actionbar
     *
     * @return Actionbar height
     */
    protected int getActionBarHeight() {
        int[] attrs = {R.attr.actionBarSize};
        TypedArray values = obtainStyledAttributes(attrs);
        try {
            return values.getDimensionPixelSize(0, 0);
        } finally {
            values.recycle();
        }
    }

    /**
     * Delete stuff from old version
     */
    private void setupNewVersion() {
        // drop database
        DatabaseManager.resetDb(this);

        // delete tumcampus directory
        File f = new File(Environment.getExternalStorageDirectory().getPath() + "/tumcampus");
        FileUtils.deleteRecursive(f);

        // Load all on start
        Utils.setInternalSetting(this, Const.EVERYTHING_SETUP, false);

        // rename hide_wizzard_on_startup
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sp.contains(Const.HIDE_WIZARD_ON_STARTUP)) {
            SharedPreferences.Editor e = sp.edit();
            e.putBoolean(Const.HIDE_WIZARD_ON_STARTUP, sp.getBoolean("hide_wizzard_on_startup", false));
            e.remove("hide_wizzard_on_startup");
            e.apply();
        }
    }
}
