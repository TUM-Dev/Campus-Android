package de.tum.`in`.tumcampusapp.component.tumui.calendar.di

import dagger.Subcomponent
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarActivity
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CreateEventActivity
import de.tum.`in`.tumcampusapp.component.tumui.calendar.widget.TimetableWidgetConfigureActivity
import de.tum.`in`.tumcampusapp.component.tumui.calendar.widget.TimetableWidgetService
import de.tum.`in`.tumcampusapp.service.SilenceService

@Subcomponent(modules = [CalendarModule::class])
interface CalendarComponent {

    fun inject(calendarActivity: CalendarActivity)

    fun inject(createEventActivity: CreateEventActivity)

    fun inject(timetableWidgetConfigureActivity: TimetableWidgetConfigureActivity)

    fun inject(timetableRemoteViewFactory: TimetableWidgetService.TimetableRemoteViewFactory)

    fun inject(silenceService: SilenceService)

    @Subcomponent.Builder
    interface Builder {

        fun calendarModule(calendarModule: CalendarModule): CalendarComponent.Builder

        fun build(): CalendarComponent

    }

}
