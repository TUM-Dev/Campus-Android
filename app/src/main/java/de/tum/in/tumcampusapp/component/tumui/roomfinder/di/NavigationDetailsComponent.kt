package de.tum.`in`.tumcampusapp.component.tumui.roomfinder.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.NavigationDetailsFragment

@Subcomponent()
interface NavigationDetailsComponent {
    fun inject(navigationDetailsFragment: NavigationDetailsFragment)
}
