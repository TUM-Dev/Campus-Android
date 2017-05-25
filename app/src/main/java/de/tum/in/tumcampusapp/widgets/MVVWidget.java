package de.tum.in.tumcampusapp.widgets;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.managers.TransportManager;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MVVWidgetConfigureActivity MVVWidgetConfigureActivity}
 */
public class MVVWidget extends AppWidgetProvider {

    private static final String BROADCAST_ALARM_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_MVVWIDGET_ALARM";
    public static final String EXTRA_STATION_ID = "de.tum.in.newtumcampus.intent.action.MVV_WIDGET_EXTRA_STATION_ID";
    private static boolean alarmIsSet = false;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        setAlarm(context);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the associated setting from the database.
        TransportManager transportManager = new TransportManager(context);
        for (int appWidgetId : appWidgetIds) {
            transportManager.deleteWidget(appWidgetId);
        }
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
        Intent intent = new Intent(context, MVVWidget.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        alarmIsSet = false;
        super.onDisabled(context);
    }

    /**
     * If no alarm is running yet a new repeating alarm is started
     */
    private static void setAlarm(Context context) {
        if (alarmIsSet) return;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MVVWidget.class);
        intent.setAction(BROADCAST_ALARM_NAME);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        //After after 30 or 60 (android may delay) seconds
        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 30000, pi);
        alarmIsSet = true;
    }

    /**
     * Updates the content of the widget
     *
     * @param appWidgetId the id of the widget to update
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        String station = context.getString(R.string.mvv_widget_no_station);
        String station_id = "";

        // Get the settings for this widget from the database
        TransportManager transportManager = new TransportManager(context);
        Cursor c = transportManager.getWidget(appWidgetId);
        if (c.getCount() >= 1) {
            c.moveToFirst();
            station = c.getString(c.getColumnIndex("station"));
            station_id = c.getString(c.getColumnIndex("station_id"));
            Boolean use_location = c.getInt(c.getColumnIndex("location")) != 0;
            c.close();
            if (use_location) {
                // TODO implement nearest station (replace the station_id string with the calculated station)
                station = "use location";
            }
        }

        // Instantiate the RemoteViews object for the app widget layout.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.mvv_widget);
        rv.setTextViewText(R.id.mvv_widget_station, station);

        // Set up the configuration activity listeners
        Intent configIntent = new Intent(context, MVVWidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.mvv_widget_header, pendingIntent);

        // Set up the intent that starts the MVVWidgetService, which will
        // provide the departure times for this station
        Intent intent = new Intent(context, MVVWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(EXTRA_STATION_ID, station_id);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setRemoteAdapter(R.id.mvv_widget_listview, intent);

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        // TODO add empty view
        //rv.setEmptyView(R.id.mvv_widget_listview, R.layout.departure_line_widget);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, rv);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.mvv_widget_listview);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        switch (intent.getAction()) {
            case BROADCAST_ALARM_NAME:
                // There may be multiple widgets active, so update all of them
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                ComponentName thisWidget = new ComponentName(context, MVVWidget.class);
                for (int widgetId : manager.getAppWidgetIds(thisWidget)) {
                    MVVWidget.updateAppWidget(context, manager, widgetId);
                }
                break;
        }
        super.onReceive(context, intent);
    }
}
