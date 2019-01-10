package de.tum.`in`.tumcampusapp.service.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradesBackgroundUpdater
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradesStore
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsStore
import de.tum.`in`.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.database.TcaDb

/**
 * This module provides dependencies that are needed in the download functionality, namely
 * [DownloadService]. It mainly includes data repositories and manager classes.
 */
@Module
class DownloadModule {

    @Provides
    fun provideTopNewsRemoteRepository(
            topNewsStore: TopNewsStore,
            tumCabeClient: TUMCabeClient
    ): TopNewsRemoteRepository = TopNewsRemoteRepository(topNewsStore, tumCabeClient)

    @Provides
    fun provideKinoLocalRepository(
            database: TcaDb
    ): KinoLocalRepository = KinoLocalRepository(database)

    @Provides
    fun provideKinoRemoteRepository(
            tumCabeClient: TUMCabeClient,
            kinoLocalRepository: KinoLocalRepository
    ): KinoRemoteRepository = KinoRemoteRepository(tumCabeClient, kinoLocalRepository)

    @Provides
    fun provideCafeteriaLocalRepository(
            database: TcaDb
    ): CafeteriaLocalRepository = CafeteriaLocalRepository(database)

    @Provides
    fun provideNewsController(
            context: Context
    ): NewsController = NewsController(context)

    @Provides
    fun provideCafeteriaMenuManager(
            context: Context
    ): CafeteriaMenuManager = CafeteriaMenuManager(context)

    @Provides
    fun provideGradesStore(
            sharedPrefs: SharedPreferences
    ): GradesStore = GradesStore(sharedPrefs)

    @Provides
    fun provideGradesBackgroundUpdater(
            context: Context,
            tumOnlineClient: TUMOnlineClient,
            notificationScheduler: NotificationScheduler,
            gradesStore: GradesStore
    ): GradesBackgroundUpdater = GradesBackgroundUpdater(
            context,
            tumOnlineClient,
            notificationScheduler,
            gradesStore
    )

}
