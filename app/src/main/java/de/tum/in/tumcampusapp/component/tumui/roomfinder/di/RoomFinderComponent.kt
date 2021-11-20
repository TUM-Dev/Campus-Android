package de.tum.`in`.tumcampusapp.component.tumui.roomfinder.di

import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderDetailsActivity

import dagger.Subcomponent

@Subcomponent()
interface RoomFinderComponent {
    fun inject(roomFinderDetailsActivity: RoomFinderDetailsActivity)
}
