package de.tum.`in`.tumcampusapp.component.ui.news.di

import dagger.Binds
import dagger.Module
import de.tum.`in`.tumcampusapp.component.ui.news.NewsDownloadAction
import de.tum.`in`.tumcampusapp.service.DownloadWorker

@Module
interface NewsModule {

    @Binds
    fun bindNewsDownloadAction(impl: NewsDownloadAction): DownloadWorker.Action

}
