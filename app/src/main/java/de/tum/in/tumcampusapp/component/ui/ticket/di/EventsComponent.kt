package de.tum.`in`.tumcampusapp.component.ui.ticket.di

import dagger.BindsInstance
import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.ticket.fragment.EventsListFragment
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType

@Subcomponent
interface EventsComponent {

    fun inject(eventsListFragment: EventsListFragment)

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun eventType(eventType: EventType): Builder

        fun build(): EventsComponent
    }
}
