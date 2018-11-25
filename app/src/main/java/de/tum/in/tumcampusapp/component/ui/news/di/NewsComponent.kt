package de.tum.`in`.tumcampusapp.component.ui.news.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.other.settings.SettingsFragment

@Subcomponent(modules = [NewsModule::class])
interface NewsComponent {

    fun inject(settingsFragment: SettingsFragment)

    @Subcomponent.Builder
    interface Builder {

        fun newsModule(newsModule: NewsModule): NewsComponent.Builder

        fun build(): NewsComponent

    }

}
