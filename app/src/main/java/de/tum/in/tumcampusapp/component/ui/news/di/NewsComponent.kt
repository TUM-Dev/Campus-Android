package de.tum.`in`.tumcampusapp.component.ui.news.di

import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.news.NewsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.news.NewsFragment
import de.tum.`in`.tumcampusapp.service.DownloadWorker

@Subcomponent(modules = [NewsModule::class])
interface NewsComponent {
    fun inject(newsFragment: NewsFragment)
}

@Module
interface NewsModule {

    @Binds
    fun bindNewsDownloadAction(impl: NewsDownloadAction): DownloadWorker.Action
}
