package de.tum.`in`.tumcampusapp.component.ui.news.di

import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.ui.news.NewsCardsProvider
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.news.repository.NewsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.news.repository.NewsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.sync.SyncManager

@Module
class NewsModule(private val context: Context) {

    @Provides
    fun provideNewsController(
            notificationScheduler: NotificationScheduler
    ): NewsController {
        return NewsController(context, notificationScheduler)
    }

    @Provides
    fun provideNewsLocalRepository(
            database: TcaDb
    ): NewsLocalRepository {
        return NewsLocalRepository(context, database)
    }

    @Provides
    fun provideNewsRemoteRepository(
            localRepository: NewsLocalRepository,
            syncManager: SyncManager,
            tumCabeClient: TUMCabeClient,
            newsController: NewsController
    ): NewsRemoteRepository {
        return NewsRemoteRepository(localRepository, syncManager, tumCabeClient, newsController)
    }

    @Provides
    fun provideNewsCardsProvider(
            database: TcaDb,
            newsLocalRepository: NewsLocalRepository
    ): NewsCardsProvider {
        return NewsCardsProvider(context, database, newsLocalRepository)
    }

    @Provides
    fun provideTopNewsRemoteRepository(
            tumCabeClient: TUMCabeClient
    ): TopNewsRemoteRepository {
        return TopNewsRemoteRepository(context, tumCabeClient)
    }

}
