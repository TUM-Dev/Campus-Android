package de.tum.`in`.tumcampusapp.component.ui.tufilm.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoActivity
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoDetailsFragment

@Subcomponent
interface KinoComponent {

    fun inject(kinoActivity: KinoActivity)

    fun inject(kinoDetailsFragment: KinoDetailsFragment)
}
