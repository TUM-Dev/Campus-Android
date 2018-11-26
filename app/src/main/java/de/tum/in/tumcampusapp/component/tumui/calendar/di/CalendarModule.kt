package de.tum.`in`.tumcampusapp.component.tumui.calendar.di

import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarCardsProvider
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.database.TcaDb

@Module
class CalendarModule(private val context: Context) {

    @Provides
    fun provideCalendarController(
            notificationScheduler: NotificationScheduler
    ): CalendarController {
        return CalendarController(context, notificationScheduler)
    }

    @Provides
    fun provideCalendarCardsProvider(
            database: TcaDb
    ): CalendarCardsProvider {
        return CalendarCardsProvider(context, database)
    }

}
