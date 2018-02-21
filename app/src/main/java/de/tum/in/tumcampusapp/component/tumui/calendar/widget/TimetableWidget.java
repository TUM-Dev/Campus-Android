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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarActivity;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity;

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
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TimetableWidget.class);
        intent.setAction(BROADCAST_UPDATE_TIMETABLE_WIDGETS);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.cancel(pi);
        am.setRepeating(AlarmManager.RTC, UPDATE_ALARM_DELAY, UPDATE_ALARM_DELAY, pi);
        alarmIsSet = true;
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
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, dd. MMM", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String title = dayFormat.format(calendar.getTime());
        rv.setTextViewText(R.id.timetable_widget_day, title);

        // Set up the configuration activity listeners
        Intent configIntent = new Intent(context, TimetableWidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingConfigIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.timetable_widget_setting, pendingConfigIntent);

        // Set up the calendar activity listeners
        Intent calendarIntent = new Intent(context, CalendarActivity.class);
        PendingIntent pendingCalendarIntent = PendingIntent.getActivity(context, appWidgetId, calendarIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.timetable_widget_day, pendingCalendarIntent);

        // Set up the roomFinder activity listeners
        Intent roomFinderIntent = new Intent(context, RoomFinderActivity.class);
        roomFinderIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent roomFinderPendingIntent = PendingIntent.getActivity(context, appWidgetId, roomFinderIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.timetable_widget_listview, roomFinderPendingIntent);

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
        super.onUpdate(context, appWidgetManager, appWidgetIds);
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
        am.cancel(sender);
        alarmIsSet = false;
        super.onDisabled(context);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        if (intent.getAction()
                  .equals(TimetableWidget.BROADCAST_UPDATE_TIMETABLE_WIDGETS)) {
            // There may be multiple widgets active, so update all of them
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, TimetableWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            updateAppWidgets(context, appWidgetManager, appWidgetIds);
        }
        super.onReceive(context, intent);
    }
}
