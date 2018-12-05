package de.tum.`in`.tumcampusapp.component.ui.tufilm.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoActivity
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoDetailsFragment

@Subcomponent(modules = [KinoModule::class])
interface KinoComponent {

    fun inject(kinoActivity: KinoActivity)

    fun inject(kinoDetailsFragment: KinoDetailsFragment)

    @Subcomponent.Builder
    interface Builder {

        fun kinoModule(kinoModule: KinoModule): KinoComponent.Builder

        fun build(): KinoComponent

    }

}
