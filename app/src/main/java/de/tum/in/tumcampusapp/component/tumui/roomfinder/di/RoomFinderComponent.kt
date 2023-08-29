package de.tum.`in`.tumcampusapp.component.tumui.roomfinder.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderDetailsActivity

@Subcomponent
interface RoomFinderComponent {
    fun inject(roomFinderDetailsActivity: RoomFinderDetailsActivity)
}
