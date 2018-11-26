package de.tum.`in`.tumcampusapp.component.tumui.calendar.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarActivity
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CreateEventActivity

@Subcomponent(modules = [CalendarModule::class])
interface CalendarComponent {

    fun inject(calendarActivity: CalendarActivity)

    fun inject(createEventActivity: CreateEventActivity)

    @Subcomponent.Builder
    interface Builder {

        fun calendarModule(calendarModule: CalendarModule): CalendarComponent.Builder

        fun build(): CalendarComponent

    }

}
