package de.tum.`in`.tumcampusapp.component.ui.ticket.di

import dagger.BindsInstance
import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.ui.ticket.fragment.EventsFragment
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType

@Subcomponent(modules = [EventsModule::class])
interface EventsComponent {

    fun inject(eventsFragment: EventsFragment)

    @Subcomponent.Builder
    interface Builder {

        fun eventsModule(eventsModule: EventsModule): EventsComponent.Builder

        @BindsInstance
        fun eventType(eventType: EventType): EventsComponent.Builder

        fun build(): EventsComponent

    }

}
