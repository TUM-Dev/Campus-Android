package de.tum.in.tumcampusapp.component.tumui.calendar.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarActivity;

public class TimetableWidget extends AppWidgetProvider {

    public final static int UPDATE_ALARM_DELAY = 30 * 60 * 1000;
    public static final String BROADCAST_UPDATE_TIMETABLE_WIDGETS = "de.tum.in.tumcampusapp.intent.action.BROADCAST_UPDATE_TIMETABLE_WIDGETS";
    private static boolean alarmIsSet;

    /**
     * If no alarm is running yet a new alarm is started which repeats every minute
     */
    private static void setAlarm(Context context) {
        if (alarmIsSet) {
            return;
        }

        Intent intent = new Intent(context, TimetableWidget.class);
        intent.setAction(BROADCAST_UPDATE_TIMETABLE_WIDGETS);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.cancel(pi);
            am.setRepeating(AlarmManager.RTC, UPDATE_ALARM_DELAY, UPDATE_ALARM_DELAY, pi);
            alarmIsSet = true;
        }
    }

    /**
     * Updates the content of multiple widgets
     *
     * @param appWidgetIds the array of widget ids to update
     */
    static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            TimetableWidget.updateAppWidget(context, appWidgetManager, widgetId);
        }
    }

    /**
     * Updates the content of the widget
     *
     * @param appWidgetId the id of the widget to update
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Instantiate the RemoteViews object for the app widget layout.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.timetable_widget);

        // Set formatted date in the header
        LocalDate localDate = DateTime.now().toLocalDate();
        String date = DateTimeFormat.longDate().print(localDate);
        rv.setTextViewText(R.id.timetable_widget_date, date);

        // Set weekday in the header
        String weekday = localDate.dayOfWeek().getAsText(Locale.getDefault());
        rv.setTextViewText(R.id.timetable_widget_weekday, weekday);

        // Set up the configuration activity listeners
        Intent configIntent = new Intent(context, TimetableWidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingConfigIntent = PendingIntent.getActivity(
                context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.timetable_widget_setting, pendingConfigIntent);

        // Set up the calendar activity listeners
        Intent calendarIntent = new Intent(context, CalendarActivity.class);
        PendingIntent pendingCalendarIntent = PendingIntent.getActivity(
                context, appWidgetId, calendarIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.timetable_widget_header, pendingCalendarIntent);

        // Set up the calendar intent used when the user taps an event
        Intent eventIntent = new Intent(context, CalendarActivity.class);
        eventIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent eventPendingIntent = PendingIntent.getActivity(
                context, appWidgetId, eventIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.timetable_widget_listview, eventPendingIntent);

        // Set up the intent that starts the TimetableWidgetService, which will
        // provide the departure times for this station
        Intent intent = new Intent(context, TimetableWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setRemoteAdapter(R.id.timetable_widget_listview, intent);

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        rv.setEmptyView(R.id.timetable_widget_listview, R.id.empty_list_item);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, rv);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.timetable_widget_listview);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAppWidgets(context, appWidgetManager, appWidgetIds);
        setAlarm(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the associated setting from the database.
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        setAlarm(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Cancel alarm as the last widget has been removed
        Intent intent = new Intent(context, TimetableWidget.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.cancel(sender);
            alarmIsSet = false;
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(TimetableWidget.BROADCAST_UPDATE_TIMETABLE_WIDGETS)) {
            // There may be multiple widgets active, so update all of them
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, TimetableWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            updateAppWidgets(context, appWidgetManager, appWidgetIds);
        }
        super.onReceive(context, intent);
    }

}
