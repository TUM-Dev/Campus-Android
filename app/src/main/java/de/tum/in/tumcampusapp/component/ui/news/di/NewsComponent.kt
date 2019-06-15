package de.tum.`in`.tumcampusapp.component.ui.news.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.news.NewsFragment

@Subcomponent(modules = [NewsModule::class])
interface NewsComponent {

    fun inject(newsFragment: NewsFragment)

}
