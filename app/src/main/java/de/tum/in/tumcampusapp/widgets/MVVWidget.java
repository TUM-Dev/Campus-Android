package de.tum.in.tumcampusapp.widgets;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Timer;
import java.util.TimerTask;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.managers.TransportManager;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MVVWidgetConfigureActivity MVVWidgetConfigureActivity}
 */
public class MVVWidget extends AppWidgetProvider {

    private static final String BROADCAST_ALARM_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_MVVWIDGET_ALARM";
    private static boolean alarmIsSet = false;
    private static Timer timer;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAppWidgets(context, appWidgetManager, appWidgetIds);
        planUpdates(context, appWidgetManager, appWidgetIds);
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
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
        alarmIsSet = false;
        super.onDisabled(context);
    }

    /**
     * If no alarm is running yet a new alarm is started which repeats every minute
     */
    private static void setAlarm(Context context) {
        if (alarmIsSet) return;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MVVWidget.class);
        intent.setAction(BROADCAST_ALARM_NAME);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.cancel(pi);
        am.setRepeating(AlarmManager.RTC, 60000, 60000, pi);
        alarmIsSet = true;
    }

    /**
     * Plans updates the widgets after 20s and 40s
     *
     * @param appWidgetIds the ids of the widgets to update
     */
    static void planUpdates(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        if (MVVWidget.timer == null) {
            MVVWidget.timer = new Timer();
        }
        MVVWidget.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateAppWidgets(context, appWidgetManager, appWidgetIds);
            }
        }, 20000);
        MVVWidget.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateAppWidgets(context, appWidgetManager, appWidgetIds);
            }
        }, 40000);
    }

    /**
     * Updates the content of multiple widgets
     *
     * @param appWidgetIds the array of widget ids to update
     */
    static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            MVVWidget.updateAppWidget(context, appWidgetManager, widgetId);
        }
    }

    /**
     * Updates the content of the widget
     *
     * @param appWidgetId the id of the widget to update
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Get the settings for this widget from the database
        TransportManager transportManager = new TransportManager(context);
        TransportManager.WidgetDepartures widgetDepartures = transportManager.getWidget(appWidgetId);

        // Instantiate the RemoteViews object for the app widget layout.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.mvv_widget);
        String station = widgetDepartures.getStation();
        if (station == null) {
            station = context.getString(R.string.mvv_widget_no_station);
        }
        rv.setTextViewText(R.id.mvv_widget_station, station);

        // Set up offline symbol (may be shown one update delayed)
        rv.setViewVisibility(R.id.mvv_widget_offline, widgetDepartures.isOffline() ? View.VISIBLE : View.INVISIBLE);

        // Set up the configuration activity listeners
        Intent configIntent = new Intent(context, MVVWidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.mvv_widget_header, pendingIntent);

        // Set up the intent that starts the MVVWidgetService, which will
        // provide the departure times for this station
        Intent intent = new Intent(context, MVVWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setRemoteAdapter(R.id.mvv_widget_listview, intent);

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        rv.setEmptyView(R.id.mvv_widget_listview, R.id.empty_list_item);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, rv);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.mvv_widget_listview);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        switch (intent.getAction()) {
            case BROADCAST_ALARM_NAME:
                // There may be multiple widgets active, so update all of them
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName thisWidget = new ComponentName(context, MVVWidget.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                updateAppWidgets(context, appWidgetManager, appWidgetIds);
                planUpdates(context, appWidgetManager, appWidgetIds);
                break;
        }
        super.onReceive(context, intent);
    }
}
