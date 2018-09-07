package de.tum.in.tumcampusapp.component.ui.onboarding;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.AuthenticationManager;
import de.tum.in.tumcampusapp.component.ui.overview.MainActivity;
import de.tum.in.tumcampusapp.service.DownloadService;
import de.tum.in.tumcampusapp.service.StartSyncReceiver;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Entrance point of the App.
 */
public class StartupActivity extends AppCompatActivity {

    private int tapCounter; // for easter egg
    private static final int REQUEST_LOCATION = 0;
    private static final String[] PERMISSIONS_LOCATION = {ACCESS_COARSE_LOCATION,
                                                          ACCESS_FINE_LOCATION};
    final AtomicBoolean initializationFinished = new AtomicBoolean(false);
    /**
     * Broadcast receiver gets notified if {@link de.tum.in.tumcampusapp.service.BackgroundService}
     * has prepared cards to be displayed
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.BROADCAST_NAME
                                         .equals(intent.getAction())) {

                //Only proceed to start the App, if initialization is finished
                if (initializationFinished.compareAndSet(false, true)) {
                    return;
                }
                startApp();
            }
        }
    };

    private void init() {

        //Migrate all settingsPrefix - we somehow ended up having two different shared prefs: join them back together
        Utils.migrateSharedPreferences(this.getApplicationContext());

        //Check that we have a private key setup in order to authenticate this device
        AuthenticationManager am = new AuthenticationManager(this);
        am.generatePrivateKey(null);

        // On first setup show remark that loading could last longer than normally
        boolean isSetup = Utils.getSettingBool(this, Const.EVERYTHING_SETUP, false);
        if (!isSetup) {
            this.runOnUiThread(() -> findViewById(R.id.startup_loading_first).setVisibility(View.VISIBLE));
        }

        // Register receiver for background service
        IntentFilter filter = new IntentFilter(DownloadService.BROADCAST_NAME);
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(receiver, filter);

        // Start background service and ensure cards are set
        Intent i = new Intent(this, StartSyncReceiver.class);
        i.putExtra(Const.APP_LAUNCHES, true);
        sendBroadcast(i);

        //Request Permissions for Android 6.0
        requestLocationPermission();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Show a loading screen during boot
        setContentView(R.layout.activity_startup);

        // init easter egg (logo)
        ImageView tumLogo = findViewById(R.id.startup_tum_logo);
        if (Utils.getSettingBool(this, Const.RAINBOW_MODE, false)) {
            tumLogo.setImageResource(R.drawable.tum_logo_rainbow);
        } else {
            tumLogo.setImageResource(R.drawable.tum_logo);
        }

        tapCounter = 0;
        View background = findViewById(R.id.startup_background);
        background.setOnClickListener(view -> {
            tapCounter++;
            if (tapCounter % 3 == 0) {
                tapCounter = 0;

                // use the other logo and invert the setting
                boolean rainbowEnabled = Utils.getSettingBool(this, Const.RAINBOW_MODE, false);
                if (rainbowEnabled) {
                    tumLogo.setImageResource(R.drawable.tum_logo);
                } else {
                    tumLogo.setImageResource(R.drawable.tum_logo_rainbow);
                }
                Utils.setSetting(this, Const.RAINBOW_MODE, !rainbowEnabled);
            }
        });
        background.setSoundEffectsEnabled(false);

        new Thread(this::init).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                             .unregisterReceiver(receiver);
    }

    /**
     * Request the Location Permission
     */
    private void requestLocationPermission() {
        //Check, if we already have permission
        if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            //We already got the permissions, to proceed normally
            //Only proceed to start the App, if initialization is finished
            if (initializationFinished.compareAndSet(false, true)) {
                return;
            }
            startApp();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION) ||
                   ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.

            // Display an AlertDialog with an explanation and a button to trigger the request.
            runOnUiThread(() -> new AlertDialog.Builder(StartupActivity.this).setMessage(getString(R.string.permission_location_explanation))
                                                                             .setPositiveButton(R.string.ok, (dialog, id) -> ActivityCompat.requestPermissions(StartupActivity.this, PERMISSIONS_LOCATION, REQUEST_LOCATION))
                                                                             .show());
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
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
        final ImageView tumLogo = findViewById(R.id.startup_tum_logo);
        final TextView loadingText = findViewById(R.id.startup_loading);
        final TextView first = findViewById(R.id.startup_loading_first);

        // Make some position calculations
        final int actionBarHeight = getActionBarHeight();
        final float screenHeight = background.getHeight();

        // Setup animation
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(background, "translationY", background.getTranslationX(), actionBarHeight - screenHeight),
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
                // NOOP
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
                // NOOP
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // NOOP
            }
        });
        set.start();
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
}
