package de.tum.`in`.tumcampusapp.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.other.locations.LocationManager
import de.tum.`in`.tumcampusapp.component.ui.news.RealTopNewsStore
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsStore
import de.tum.`in`.tumcampusapp.component.ui.overview.CardsRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventCardsProvider
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import javax.inject.Singleton

/**
 * This module provides dependencies that are needed throughout the entire app, for instance the
 * database or the Retrofit clients.
 *
 * @param context A [Context]
 */
@Module
class AppModule(private val context: Context) {

    @Singleton
    @Provides
    fun provideContext(): Context = context

    @Singleton
    @Provides
    fun provideSharedPreferences(
            context: Context
    ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Singleton
    @Provides
    fun provideTopNewsStore(
            sharedPrefs: SharedPreferences
    ): TopNewsStore = RealTopNewsStore(sharedPrefs)

    @Singleton
    @Provides
    fun provideTUMCabeClient(
            context: Context
    ): TUMCabeClient = TUMCabeClient.getInstance(context)

    @Singleton
    @Provides
    fun provideTUMOnlineClient(
            context: Context
    ): TUMOnlineClient = TUMOnlineClient.getInstance(context)

    @Singleton
    @Provides
    fun provideDatabase(
            context: Context
    ): TcaDb = TcaDb.getInstance(context)

    @Singleton
    @Provides
    fun provideLocationManager(
            context: Context
    ): LocationManager = LocationManager(context)

    @Singleton
    @Provides
    fun provideEventsLocalRepository(
            database: TcaDb
    ): EventsLocalRepository = EventsLocalRepository(database)

    @Singleton
    @Provides
    fun provideEventCardsProvider(
            context: Context,
            eventsLocalRepository: EventsLocalRepository
    ): EventCardsProvider = EventCardsProvider(context, eventsLocalRepository)

    @Singleton
    @Provides
    fun provideCardsRepository(
            context: Context,
            eventCardsProvider: EventCardsProvider
    ): CardsRepository = CardsRepository(context, eventCardsProvider)

    @Singleton
    @Provides
    fun provideAuthenticationManager(
            context: Context
    ): AuthenticationManager = AuthenticationManager(context)

    @Singleton
    @Provides
    fun provideNotificationScheduler(
            context: Context
    ): NotificationScheduler = NotificationScheduler(context)

}
