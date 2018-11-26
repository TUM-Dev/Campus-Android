package de.tum.`in`.tumcampusapp.component.ui.transportation.di

import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.other.locations.TumLocationManager
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportCardsProvider
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportNotificationProvider
import de.tum.`in`.tumcampusapp.component.ui.transportation.api.MvvApiService
import de.tum.`in`.tumcampusapp.component.ui.transportation.repository.TransportLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.transportation.repository.TransportRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.transportation.widget.MVVWidgetController
import de.tum.`in`.tumcampusapp.database.TcaDb

@Module
class TransportModule(private val context: Context) {

    @Provides
    fun provideTransportController(
            notificationScheduler: NotificationScheduler
    ): TransportController {
        return TransportController(context, notificationScheduler)
    }

    @Provides
    fun provideTransportLocalRepository(
            database: TcaDb
    ): TransportLocalRepository {
        return TransportLocalRepository(database)
    }

    @Provides
    fun provideTransportRemoteRepository(
            mvvService: MvvApiService
    ): TransportRemoteRepository {
        return TransportRemoteRepository(mvvService)
    }

    @Provides
    fun provideTransportCardsProvider(
            transportRemoteRepository: TransportRemoteRepository,
            tumLocationManager: TumLocationManager
    ): TransportCardsProvider {
        return TransportCardsProvider(context, transportRemoteRepository, tumLocationManager)
    }

    @Provides
    fun provideMVVWidgetController(
            localRepository: TransportLocalRepository
    ): MVVWidgetController {
        return MVVWidgetController(localRepository)
    }

    @Provides
    fun provideTransportNotificationProvider(
            remoteRepository: TransportRemoteRepository,
            tumLocationManager: TumLocationManager
    ): TransportNotificationProvider {
        return TransportNotificationProvider(context, remoteRepository, tumLocationManager)
    }

}
