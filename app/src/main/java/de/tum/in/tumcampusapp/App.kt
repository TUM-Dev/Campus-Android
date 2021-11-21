package de.tum.`in`.tumcampusapp

import android.app.Application
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import de.tum.`in`.tumcampusapp.component.notifications.NotificationUtils.setupNotificationChannels
import de.tum.`in`.tumcampusapp.component.other.settings.ThemeProvider
import de.tum.`in`.tumcampusapp.di.AppComponent
import de.tum.`in`.tumcampusapp.di.DaggerAppComponent
import io.reactivex.plugins.RxJavaPlugins
import net.danlew.android.joda.JodaTimeAndroid

open class App : Application() {

    // If we run Robolectric unit tests, this variable will be true
    open val testing = false

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        buildAppComponent()
        setupPicasso()
        setupNotificationChannels(this)
        JodaTimeAndroid.init(this)
        if (!testing) {
            initRxJavaErrorHandler()
        }
        setupStrictMode()
        loadTheme()
    }

    private fun buildAppComponent() {
        // We use Dagger 2 for dependency injection. The main AppModule and AppComponent can be
        // found in the package "di".
        appComponent = DaggerAppComponent.builder()
                .context(this)
                .build()
    }

    protected open fun setupPicasso() {
        val builder = Picasso.Builder(this)
        builder.downloader(OkHttp3Downloader(this, Integer.MAX_VALUE.toLong()))

        val built = builder.build()
        built.isLoggingEnabled = true

        Picasso.setSingletonInstance(built)
    }

    protected fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .permitDiskReads() // These are mainly caused by shared preferences and room. Probably enable
                    .permitDiskWrites() // this as soon as we don't call allowMainThreadQueries() in TcaDb
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    // .detectLeakedClosableObjects() // seems like room / DAOs leak
                    .detectLeakedRegistrationObjects()
                    .detectFileUriExposure()
                    // .detectCleartextNetwork() // not available at the current minSdk
                    // .detectContentUriWithoutPermission()
                    // .detectUntaggedSockets()
                    .penaltyLog()
                    .build())
        }
    }

    private fun initRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(FirebaseCrashlytics.getInstance()::recordException)
    }

    private fun loadTheme() {
        val theme = ThemeProvider(this).getThemeFromPreferences()
        AppCompatDelegate.setDefaultNightMode(theme)
    }
}
