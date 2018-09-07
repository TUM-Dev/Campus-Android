package de.tum.in.tumcampusapp;

import android.app.Application;
import android.os.StrictMode;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import net.danlew.android.joda.JodaTimeAndroid;

import de.tum.in.tumcampusapp.component.notifications.NotificationUtils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        setupPicasso();
        NotificationUtils.setupNotificationChannels(this);
        JodaTimeAndroid.init(this);
        setupStrictMode();
    }

    protected void setupPicasso() {
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));

        Picasso built = builder.build();
        built.setLoggingEnabled(true);

        if (BuildConfig.DEBUG) {
            built.setIndicatorsEnabled(true);
        }

        Picasso.setSingletonInstance(built);
    }

    protected void setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                                               .detectAll()
                                               .permitDiskReads()  // those is mainly caused by shared preferences and room. probably enable
                                               .permitDiskWrites() // this as soon as we don't call allowMainThreadQueries() in TcaDb
                                               .penaltyLog()
                                               .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                                           .detectActivityLeaks()
                                           //.detectLeakedClosableObjects() // seems like room / DAOs leak
                                           .detectLeakedRegistrationObjects()
                                           .detectFileUriExposure()
                                           //.detectCleartextNetwork() // not available at the current minSdk
                                           //.detectContentUriWithoutPermission()
                                           //.detectUntaggedSockets()
                                           .penaltyLog()
                                           .build());
        }
    }
}
