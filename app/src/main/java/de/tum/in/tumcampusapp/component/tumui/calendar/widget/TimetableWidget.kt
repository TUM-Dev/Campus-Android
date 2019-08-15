package de.tum.`in`.tumcampusapp.component.tumui.calendar.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarActivity
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class TimetableWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAppWidgets(context, appWidgetManager, appWidgetIds)
        setAlarm(context)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        setAlarm(context)
    }

    override fun onDisabled(context: Context) {
        // Cancel alarm as the last widget has been removed
        val intent = Intent(context, TimetableWidget::class.java)
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        if (alarmManager != null) {
            alarmManager.cancel(sender)
            alarmIsSet = false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null && action == BROADCAST_UPDATE_TIMETABLE_WIDGETS) {
            // There may be multiple widgets active, so update all of them
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, TimetableWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            updateAppWidgets(context, appWidgetManager, appWidgetIds)
        }
        super.onReceive(context, intent)
    }

    companion object {

        private const val UPDATE_ALARM_DELAY = 30 * 60 * 1000
        const val BROADCAST_UPDATE_TIMETABLE_WIDGETS = "de.tum.in.tumcampusapp.intent.action.BROADCAST_UPDATE_TIMETABLE_WIDGETS"
        private var alarmIsSet: Boolean = false

        /**
         * If no alarm is running yet a new alarm is started which repeats every minute
         */
        private fun setAlarm(context: Context) {
            if (alarmIsSet) {
                return
            }

            val intent = Intent(context, TimetableWidget::class.java)
            intent.action = BROADCAST_UPDATE_TIMETABLE_WIDGETS
            val pi = PendingIntent.getBroadcast(context, 0, intent, 0)

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
            if (am != null) {
                am.cancel(pi)
                am.setRepeating(AlarmManager.RTC, UPDATE_ALARM_DELAY.toLong(), UPDATE_ALARM_DELAY.toLong(), pi)
                alarmIsSet = true
            }
        }

        /**
         * Updates the content of multiple widgets
         *
         * @param appWidgetIds the array of widget ids to update
         */
        internal fun updateAppWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            for (widgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }

        /**
         * Updates the content of the widget
         *
         * @param appWidgetId the id of the widget to update
         */
        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {
            // Instantiate the RemoteViews object for the app widget layout.
            val remoteViews = RemoteViews(context.packageName, R.layout.timetable_widget)

            // Set formatted date in the header
            val localDate = DateTime.now().toLocalDate()
            val date = DateTimeFormat.longDate().print(localDate)
            remoteViews.setTextViewText(R.id.timetable_widget_date, date)

            // Set weekday in the header
            val weekday = localDate.dayOfWeek().getAsText(Locale.getDefault())
            remoteViews.setTextViewText(R.id.timetable_widget_weekday, weekday)

            // Set up the configuration activity listeners
            val configIntent = Intent(context, TimetableWidgetConfigureActivity::class.java)
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val pendingConfigIntent = PendingIntent.getActivity(
                    context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.timetable_widget_setting, pendingConfigIntent)

            // Set up the calendar activity listeners
            val calendarIntent = Intent(context, CalendarActivity::class.java)
            val pendingCalendarIntent = PendingIntent.getActivity(
                    context, appWidgetId, calendarIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.timetable_widget_header, pendingCalendarIntent)

            // Set up the calendar intent used when the user taps an event
            val eventIntent = Intent(context, CalendarActivity::class.java)
            eventIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val eventPendingIntent = PendingIntent.getActivity(
                    context, appWidgetId, eventIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setPendingIntentTemplate(R.id.timetable_widget_listview, eventPendingIntent)

            // Set up the intent that starts the TimetableWidgetService, which will
            // provide the departure times for this station
            val intent = Intent(context, TimetableWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            remoteViews.setRemoteAdapter(R.id.timetable_widget_listview, intent)

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            remoteViews.setEmptyView(R.id.timetable_widget_listview, R.id.empty_list_item)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.timetable_widget_listview)
        }
    }

}
