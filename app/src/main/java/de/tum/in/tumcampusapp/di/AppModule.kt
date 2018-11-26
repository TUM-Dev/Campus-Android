package de.tum.`in`.tumcampusapp.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.cafeteria.CafeteriaAPIClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.other.locations.LocationProvider
import de.tum.`in`.tumcampusapp.component.other.locations.TumLocationManager
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeeManager
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeesCardsProvider
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeesNotificationProvider
import de.tum.`in`.tumcampusapp.component.ui.eduroam.EduroamController
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportCardsProvider
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportNotificationProvider
import de.tum.`in`.tumcampusapp.component.ui.transportation.api.MvvApiService
import de.tum.`in`.tumcampusapp.component.ui.transportation.api.MvvClient
import de.tum.`in`.tumcampusapp.component.ui.transportation.repository.TransportLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.transportation.repository.TransportRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.transportation.widget.MVVWidgetController
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoUpdater
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoRemoteRepository
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
    fun provideTumLocationManager(): TumLocationManager {
        return TumLocationManager(context)
    }

    @Singleton
    @Provides
    fun provideLocationProvider(): LocationProvider {
        return LocationProvider(context)
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
    fun provideTransportController(): TransportController {
        return TransportController(context)
    }

    @Singleton
    @Provides
    fun provideTransportLocalRepository(
            database: TcaDb
    ): TransportLocalRepository {
        return TransportLocalRepository(database)
    }

    @Singleton
    @Provides
    fun provideTransportRemoteRepository(
            mvvService: MvvApiService
    ): TransportRemoteRepository {
        return TransportRemoteRepository(mvvService)
    }

    @Singleton
    @Provides
    fun provideTransportCardsProvider(
            transportRemoteRepository: TransportRemoteRepository
    ): TransportCardsProvider {
        return TransportCardsProvider(context, transportRemoteRepository)
    }

    @Singleton
    @Provides
    fun provideMVVWidgetController(
            localRepository: TransportLocalRepository
    ): MVVWidgetController {
        return MVVWidgetController(localRepository)
    }

    @Singleton
    @Provides
    fun provideTransportNotificationProvider(
            remoteRepository: TransportRemoteRepository,
            tumLocationManager: TumLocationManager
    ): TransportNotificationProvider {
        return TransportNotificationProvider(context, remoteRepository, tumLocationManager)
    }

    @Singleton
    @Provides
    fun provideTuitionFeeManager(): TuitionFeeManager {
        return TuitionFeeManager(context)
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
    fun provideTuitionFeesNotificationProvider(): TuitionFeesNotificationProvider {
        return TuitionFeesNotificationProvider(context)
    }

    @Singleton
    @Provides
    fun provideEduroamController(): EduroamController {
        return EduroamController(context)
    }

    @Singleton
    @Provides
    fun provideKinoLocalRepository(
            database: TcaDb
    ): KinoLocalRepository {
        return KinoLocalRepository(database)
    }

    @Singleton
    @Provides
    fun provideKinoRemoteRepository(
            tumCabeClient: TUMCabeClient
    ): KinoRemoteRepository {
        return KinoRemoteRepository(tumCabeClient)
    }

    @Singleton
    @Provides
    fun provideKinoUpdater(
            localRepository: KinoLocalRepository,
            remoteRepository: KinoRemoteRepository
    ): KinoUpdater {
        return KinoUpdater(localRepository, remoteRepository)
    }

}
