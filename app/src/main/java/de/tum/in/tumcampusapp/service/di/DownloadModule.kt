package de.tum.`in`.tumcampusapp.service.di

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.IdUploadAction
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradesBackgroundUpdater
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradesDownloadAction
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradesStore
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaLocationImportAction
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.news.NewsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsStore
import de.tum.`in`.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.FilmDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.service.DownloadWorker

/**
 * This module provides dependencies that are needed in the download functionality, namely
 * [DownloadWorker]. It mainly includes data repositories and manager classes.
 */
@Module
class DownloadModule {

    @Provides
    fun provideAssetManager(
            context: Context
    ): AssetManager = context.assets

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
    fun provideCafeteriaDownloadAction(
            menuManager: CafeteriaMenuManager,
            remoteRepository: CafeteriaRemoteRepository
    ): CafeteriaDownloadAction = CafeteriaDownloadAction(menuManager, remoteRepository)

    @Provides
    fun provideCafeteriaLocationImportAction(
            assetManager: AssetManager,
            database: TcaDb
    ): CafeteriaLocationImportAction = CafeteriaLocationImportAction(assetManager, database)

    @Provides
    fun provideEventsDownloadAction(
            remoteRepository: EventsRemoteRepository
    ): EventsDownloadAction = EventsDownloadAction(remoteRepository)

    @Provides
    fun provideFilmDownloadAction(
            remoteRepository: KinoRemoteRepository
    ): FilmDownloadAction = FilmDownloadAction(remoteRepository)

    @Provides
    fun provideIdUploadAction(
            context: Context,
            authManager: AuthenticationManager,
            tumCabeClient: TUMCabeClient
    ): IdUploadAction = IdUploadAction(context, authManager, tumCabeClient)

    @Provides
    fun provideNewsDownloadAction(
            newsController: NewsController
    ): NewsDownloadAction = NewsDownloadAction(newsController)

    @Provides
    fun provideTopNewsDownloadAction(
            remoteRepository: TopNewsRemoteRepository
    ): TopNewsDownloadAction = TopNewsDownloadAction(remoteRepository)

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

    @Provides
    fun provideGradesDownloadAction(
            updater: GradesBackgroundUpdater
    ): GradesDownloadAction = GradesDownloadAction(updater)

    @Provides
    fun provideWorkerActions(
            cafeteriaDownloadAction: CafeteriaDownloadAction,
            cafeteriaLocationImportAction: CafeteriaLocationImportAction,
            eventsDownloadAction: EventsDownloadAction,
            filmDownloadAction: FilmDownloadAction,
            gradesDownloadAction: GradesDownloadAction,
            idUploadAction: IdUploadAction,
            newsDownloadAction: NewsDownloadAction,
            topNewsDownloadAction: TopNewsDownloadAction
    ): DownloadWorker.WorkerActions = DownloadWorker.WorkerActions(
            cafeteriaDownloadAction,
            cafeteriaLocationImportAction,
            eventsDownloadAction,
            filmDownloadAction,
            gradesDownloadAction,
            idUploadAction,
            newsDownloadAction,
            topNewsDownloadAction
    )

}
