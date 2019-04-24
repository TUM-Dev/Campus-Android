package de.tum.`in`.tumcampusapp.component.ui.news.di

import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.news.NewsDownloadAction
import de.tum.`in`.tumcampusapp.service.DownloadWorker

@Module
class NewsModule {

    @Provides
    fun provideNewsController(
            context: Context
    ): NewsController = NewsController(context)

    @Provides
    fun provideNewsDownloadAction(
            newsController: NewsController
    ): DownloadWorker.Action = NewsDownloadAction(newsController)

}
