package de.tum.`in`.tumcampusapp.component.tumui.roomfinder.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderDetailsActivity

@Subcomponent
@Deprecated("""Please use NavigationDetailsComponent instead""")
interface RoomFinderComponent {
    fun inject(roomFinderDetailsActivity: RoomFinderDetailsActivity)
}
