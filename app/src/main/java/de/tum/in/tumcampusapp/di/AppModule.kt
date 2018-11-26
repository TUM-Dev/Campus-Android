package de.tum.`in`.tumcampusapp.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.cafeteria.CafeteriaAPIClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.other.locations.LocationProvider
import de.tum.`in`.tumcampusapp.component.other.locations.TumLocationManager
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeeManager
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeesCardsProvider
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeesNotificationProvider
import de.tum.`in`.tumcampusapp.component.ui.eduroam.EduroamController
import de.tum.`in`.tumcampusapp.component.ui.transportation.api.MvvApiService
import de.tum.`in`.tumcampusapp.component.ui.transportation.api.MvvClient
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.sync.SyncManager
import org.jetbrains.anko.defaultSharedPreferences
import javax.inject.Singleton

@Module
class AppModule(private val context: Context) {

    @Singleton
    @Provides
    fun provideContext(): Context {
        return context
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(): SharedPreferences {
        return context.defaultSharedPreferences
    }

    @Singleton
    @Provides
    fun provideAuthenticationManager(): AuthenticationManager {
        return AuthenticationManager(context)
    }

    @Singleton
    @Provides
    fun provideDatabase(): TcaDb {
        return TcaDb.getInstance(context)
    }

    @Singleton
    @Provides
    fun providesTumCabeClient(): TUMCabeClient {
        return TUMCabeClient.getInstance(context)
    }

    @Singleton
    @Provides
    fun providesTumOnlineClient(): TUMOnlineClient {
        return TUMOnlineClient.getInstance(context)
    }

    @Provides
    fun provideCafeteriaApiClient(): CafeteriaAPIClient {
        return CafeteriaAPIClient.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideTumLocationManager(
            notificationScheduler: NotificationScheduler,
            calendarController: CalendarController
    ): TumLocationManager {
        return TumLocationManager(context, notificationScheduler, calendarController)
    }

    @Singleton
    @Provides
    fun provideLocationProvider(): LocationProvider {
        return LocationProvider(context)
    }

    @Singleton
    @Provides
    fun provideNotificationScheduler(): NotificationScheduler {
        return NotificationScheduler(context)
    }

    @Singleton
    @Provides
    fun provideSyncManager(): SyncManager {
        return SyncManager(context)
    }

    @Singleton
    @Provides
    fun provideMvvApiService(): MvvApiService {
        return MvvClient.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideTuitionFeeManager(
            notificationScheduler: NotificationScheduler
    ): TuitionFeeManager {
        return TuitionFeeManager(context, notificationScheduler)
    }

    @Singleton
    @Provides
    fun provideTuitionFeesCardsProvider(
            tuitionFeeManager: TuitionFeeManager
    ): TuitionFeesCardsProvider {
        return TuitionFeesCardsProvider(context, tuitionFeeManager)
    }

    @Singleton
    @Provides
    fun provideTuitionFeesNotificationProvider(
            tuitionFeeManager: TuitionFeeManager
    ): TuitionFeesNotificationProvider {
        return TuitionFeesNotificationProvider(context, tuitionFeeManager)
    }

    @Singleton
    @Provides
    fun provideEduroamController(): EduroamController {
        return EduroamController(context)
    }

}
