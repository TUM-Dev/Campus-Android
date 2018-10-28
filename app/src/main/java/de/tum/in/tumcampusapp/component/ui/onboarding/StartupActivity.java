package de.tum.in.tumcampusapp.component.ui.onboarding;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.work.State;
import androidx.work.WorkStatus;
import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.AuthenticationManager;
import de.tum.in.tumcampusapp.component.ui.overview.MainActivity;
import de.tum.in.tumcampusapp.service.StartSyncReceiver;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.fabric.sdk.android.Fabric;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Entrance point of the App.
 */
public class StartupActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 0;
    private static final String[] PERMISSIONS_LOCATION = {ACCESS_COARSE_LOCATION,
                                                          ACCESS_FINE_LOCATION};

    final AtomicBoolean initializationFinished = new AtomicBoolean(false);
    private int tapCounter; // for easter egg

    @Nullable
    LiveData<WorkStatus> downloadStatus;

    // TODO: register background download finished WorkStatus
    Observer<WorkStatus> downloadObserver = workStatus -> {
        if (workStatus == null || workStatus.getState() != State.SUCCEEDED) {
            return;
        }
        openMainActivityIfInitializationFinished();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        // Only use Crashlytics if we are not compiling debug
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        initEasterEgg();

        new Thread(this::init).start();
    }

    private void initEasterEgg() {
        if (Utils.getSettingBool(this, Const.RAINBOW_MODE, false)) {
            ImageView tumLogo = findViewById(R.id.startupTumLogo);
            tumLogo.setImageResource(R.drawable.tum_logo_rainbow);
        }

        tapCounter = 0;
        View background = findViewById(R.id.container);
        background.setOnClickListener(view -> {
            tapCounter++;
            if (tapCounter % 3 == 0) {
                tapCounter = 0;

                // use the other logo and invert the setting
                boolean rainbowEnabled = Utils.getSettingBool(this, Const.RAINBOW_MODE, false);
                rainbowEnabled = !rainbowEnabled;
                ImageView tumLogo = findViewById(R.id.startupTumLogo);

                if (rainbowEnabled) {
                    tumLogo.setImageResource(R.drawable.tum_logo_rainbow);
                } else {
                    tumLogo.setImageResource(R.drawable.tum_logo_blue);
                }

                Utils.setSetting(this, Const.RAINBOW_MODE, rainbowEnabled);
            }
        });
        background.setSoundEffectsEnabled(false);
    }

    private void init() {
        // Migrate all settingsPrefix - we somehow ended up having two different shared prefs: join them back together
        Utils.migrateSharedPreferences(this.getApplicationContext());

        // Check that we have a private key setup in order to authenticate this device
        AuthenticationManager am = new AuthenticationManager(this);
        am.generatePrivateKey(null);

        // On first setup show remark that loading could last longer than normally
        runOnUiThread(() -> {
            ContentLoadingProgressBar progressBar = findViewById(R.id.startupLoadingProgressBar);
            progressBar.show();
        });

        // TODO: manually start DownloadWorker to listen to success
        // Start background service and ensure cards are set
        Intent i = new Intent(this, StartSyncReceiver.class);
        i.putExtra(Const.APP_LAUNCHES, true);
        sendBroadcast(i);

        // Request Permissions for Android 6.0
        requestLocationPermission();
    }

    /**
     * Request the Location Permission
     */
    private void requestLocationPermission() {
        //Check, if we already have permission
        if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            // We already got the permissions, to proceed normally
            openMainActivityIfInitializationFinished();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.

            // Display an AlertDialog with an explanation and a button to trigger the request.
            runOnUiThread(() -> {
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.permission_location_explanation))
                        .setPositiveButton(R.string.ok, (dialogInterface, id) -> {
                            ActivityCompat.requestPermissions(
                                    this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
                        })
                        .create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(
                            R.drawable.rounded_corners_background);
                }

                dialog.show();
            });
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        openMainActivityIfInitializationFinished();
    }

    private void openMainActivityIfInitializationFinished() {
        if (initializationFinished.compareAndSet(false, true) || isFinishing()) {
            // If the initialization process is not yet finished or if the Activity is
            // already being finished, there's no need to open MainActivity.
            return;
        }
        openMainActivity();
    }

    /**
     * Animates the TUM logo into place (left upper corner) and animates background up.
     * Afterwards {@link MainActivity} gets started
     */
    private void openMainActivity() {
        Intent intent = new Intent(StartupActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
