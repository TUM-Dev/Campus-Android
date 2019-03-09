package de.tum.`in`.tumcampusapp.component.ui.news.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.news.NewsActivity

@Subcomponent(modules = [NewsModule::class])
interface NewsComponent {

    fun inject(newsActivity: NewsActivity)

    @Subcomponent.Builder
    interface Builder {

        fun newsModule(newsModule: NewsModule): NewsComponent.Builder

        fun build(): NewsComponent

    }

}
