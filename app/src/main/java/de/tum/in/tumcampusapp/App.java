package de.tum.in.tumcampusapp;

import android.app.Application;
import android.os.StrictMode;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import net.danlew.android.joda.JodaTimeAndroid;

import de.tum.in.tumcampusapp.di.AppComponent;
import de.tum.in.tumcampusapp.di.AppModule;
import de.tum.in.tumcampusapp.di.DaggerAppComponent;
import io.reactivex.plugins.RxJavaPlugins;

import static de.tum.in.tumcampusapp.component.notifications.NotificationUtils.setupNotificationChannels;

public class App extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        buildAppComponent();
        setupPicasso();
        setupNotificationChannels(this);
        JodaTimeAndroid.init(this);
        initRxJavaErrorHandler();
        setupStrictMode();
    }

    private void buildAppComponent() {
        // We use Dagger 2 for dependency injection. The main AppModule and AppComponent can be
        // found in the package "di".
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    protected void setupPicasso() {
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));

        Picasso built = builder.build();
        built.setLoggingEnabled(true);

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

    private void initRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(Crashlytics::logException);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
