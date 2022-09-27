package de.tum.`in`.tumcampusapp.component.ui.search.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.search.SearchFragment

@Subcomponent()
interface SearchComponent {
    fun inject(searchFragment: SearchFragment)
}
