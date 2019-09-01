package de.tum.`in`.tumcampusapp.component.tumui.calendar.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.calendar.WidgetCalendarItem
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.collections.ArrayList

class TimetableWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TimetableRemoteViewFactory(this.applicationContext, intent)
    }

    private class TimetableRemoteViewFactory internal constructor(
        private val applicationContext: Context,
        intent: Intent
    ) : RemoteViewsFactory {

        private val appWidgetID: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        private var calendarEvents: List<WidgetCalendarItem> = ArrayList()

        override fun onCreate() {}

        override fun onDataSetChanged() {
            // Get events
            val calendarController = CalendarController(this.applicationContext)
            calendarEvents = calendarController.getNextDaysFromDb(14, this.appWidgetID)

            // Set isFirstOnDay flags
            if (calendarEvents.isNotEmpty()) {
                calendarEvents[0].isFirstOnDay = true
            }

            for (index in 1 until calendarEvents.size) {
                val (_, _, startTime) = calendarEvents[index - 1]
                val lastTime = DateTime(startTime.millis)

                val thisEvent = calendarEvents[index]
                val thisTime = DateTime(thisEvent.startTime.millis)

                if (!DateTimeUtils.isSameDay(lastTime, thisTime)) {
                    thisEvent.isFirstOnDay = true
                }
            }
        }

        override fun onDestroy() {}

        override fun getCount() = calendarEvents.size

        override fun getViewAt(position: Int): RemoteViews? {
            if (position >= calendarEvents.size) {
                return null
            }

            val remoteViews = RemoteViews(applicationContext.packageName, R.layout.timetable_widget_item)

            // Get the lecture for this view
            val currentItem = this.calendarEvents[position]
            val startTime = DateTime(currentItem.startTime.millis)

            // Setup the date
            if (currentItem.isFirstOnDay) {
                remoteViews.setTextViewText(R.id.timetable_widget_date_day, startTime.dayOfMonth.toString())
                remoteViews.setTextViewText(R.id.timetable_widget_date_weekday, startTime.dayOfWeek()
                        .getAsShortText(Locale.getDefault()))
                remoteViews.setViewPadding(R.id.timetable_widget_item, 0, 15, 0, 0)
            } else {
                // Overwrite unused parameters, as the elements are reused they may could be filled with old parameters
                remoteViews.setTextViewText(R.id.timetable_widget_date_day, "")
                remoteViews.setTextViewText(R.id.timetable_widget_date_weekday, "")
                remoteViews.setViewPadding(R.id.timetable_widget_item, 0, 0, 0, 0)
            }

            // TODO: Display month label if event is the first event in a new month

            // Setup event color
            remoteViews.setInt(R.id.timetable_widget_event, "setBackgroundColor", currentItem.color)

            // Setup event title
            remoteViews.setTextViewText(R.id.timetable_widget_event_title, currentItem.title)

            // Setup event time
            val formatter = DateTimeFormat.shortTime()
            val startTimeText = formatter.print(startTime)
            val endTime = DateTime(currentItem.endTime.millis)
            val endTimeText = formatter.print(endTime)
            val eventTime = applicationContext.getString(R.string.event_start_end_format_string, startTimeText, endTimeText)
            remoteViews.setTextViewText(R.id.timetable_widget_event_time, eventTime)

            // Setup event location
            remoteViews.setTextViewText(R.id.timetable_widget_event_location, currentItem.location)

            // Setup action to open calendar
            val fillInIntent = Intent().apply {
                putExtra(Const.EVENT_TIME, currentItem.startTime.millis)
            }
            remoteViews.setOnClickFillInIntent(R.id.timetable_widget_event, fillInIntent)

            return remoteViews
        }

        override fun getLoadingView() = null

        override fun getViewTypeCount() = 1

        override fun getItemId(position: Int) = position.toLong()

        override fun hasStableIds() = true
    }
}
