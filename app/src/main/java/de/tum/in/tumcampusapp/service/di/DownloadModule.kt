package de.tum.`in`.tumcampusapp.service.di

import android.content.Context
import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.IdUploadAction
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradesBackgroundUpdater
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradesDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.news.NewsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.openinghour.LocationImportAction
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.FilmDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.updatenote.UpdateNoteDownloadAction
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.service.DownloadWorker

/**
 * This module provides dependencies that are needed in the download functionality, namely
 * [DownloadWorker]. It mainly includes data repositories and manager classes.
 */
@Module
object DownloadModule {

    @JvmStatic
    @Provides
    fun provideAssetManager(
            context: Context
    ): AssetManager = context.assets

    @JvmStatic
    @Provides
    fun provideCafeteriaDownloadAction(
            menuManager: CafeteriaMenuManager,
            remoteRepository: CafeteriaRemoteRepository
    ): CafeteriaDownloadAction = CafeteriaDownloadAction(menuManager, remoteRepository)

    @JvmStatic
    @Provides
    fun provideLocationImportAction(
            context: Context,
            database: TcaDb,
            tumCabeClient: TUMCabeClient
    ): LocationImportAction = LocationImportAction(context, database, tumCabeClient)

    @JvmStatic
    @Provides
    fun provideEventsDownloadAction(
            remoteRepository: EventsRemoteRepository
    ): EventsDownloadAction = EventsDownloadAction(remoteRepository)

    @JvmStatic
    @Provides
    fun provideFilmDownloadAction(
            remoteRepository: KinoRemoteRepository
    ): FilmDownloadAction = FilmDownloadAction(remoteRepository)

    @JvmStatic
    @Provides
    fun provideIdUploadAction(
            context: Context,
            authManager: AuthenticationManager,
            tumCabeClient: TUMCabeClient
    ): IdUploadAction = IdUploadAction(context, authManager, tumCabeClient)

    @JvmStatic
    @Provides
    fun provideNewsDownloadAction(
            newsController: NewsController
    ): NewsDownloadAction = NewsDownloadAction(newsController)

    @JvmStatic
    @Provides
    fun provideTopNewsDownloadAction(
            remoteRepository: TopNewsRemoteRepository
    ): TopNewsDownloadAction = TopNewsDownloadAction(remoteRepository)

    @JvmStatic
    @Provides
    fun provideUpdateNoteDownloadAction(
            context: Context
    ): UpdateNoteDownloadAction = UpdateNoteDownloadAction(context)

    @JvmStatic
    @Provides
    fun provideGradesDownloadAction(
            updater: GradesBackgroundUpdater
    ): GradesDownloadAction = GradesDownloadAction(updater)

    @JvmStatic
    @Provides
    fun provideWorkerActions(
            cafeteriaDownloadAction: CafeteriaDownloadAction,
            locationImportAction: LocationImportAction,
            eventsDownloadAction: EventsDownloadAction,
            filmDownloadAction: FilmDownloadAction,
            gradesDownloadAction: GradesDownloadAction,
            idUploadAction: IdUploadAction,
            newsDownloadAction: NewsDownloadAction,
            topNewsDownloadAction: TopNewsDownloadAction,
            updateNoteDownloadAction: UpdateNoteDownloadAction
    ): DownloadWorker.WorkerActions = DownloadWorker.WorkerActions(
            cafeteriaDownloadAction,
            locationImportAction,
            eventsDownloadAction,
            filmDownloadAction,
            gradesDownloadAction,
            idUploadAction,
            newsDownloadAction,
            topNewsDownloadAction,
            updateNoteDownloadAction
    )

}
