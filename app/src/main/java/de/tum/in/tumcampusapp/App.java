package de.tum.in.tumcampusapp;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setupPicasso();
        setupCrashlytics();
    }

    private void setupPicasso() {
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));

        Picasso built = builder.build();
        built.setLoggingEnabled(true);

        if (BuildConfig.DEBUG) {
            built.setIndicatorsEnabled(true);
        }

        Picasso.setSingletonInstance(built);
    }

    /**
     * Disable crashlytics for debug builds, or non-release builds indicated by the "dev" suffix
     */
    private void setupCrashlytics() {

        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG || BuildConfig.VERSION_NAME.contains("dev"))
                                                   .build())
                .build();

        Fabric.with(this, crashlyticsKit);
    }
}
