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

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.managers.TransportManager;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MVVWidgetConfigureActivity MVVWidgetConfigureActivity}
 */
public class MVVWidget extends AppWidgetProvider {

    private static final String BROADCAST_RELOAD_ALL = "de.tum.in.newtumcampus.intent.action.BROADCAST_MVV_WIDGET_RELOAD_ALL";
    static final String MVV_WIDGET_FORCE_RELOAD = "de.tum.in.newtumcampus.intent.action.MVV_WIDGET_FORCE_RELOAD";
    private static Timer timer;
    private static TransportManager transportManager;

    public final static int UPDATE_ALARM_DELAY = 60 * 1000;
    public final static int DOWNLOAD_DELAY = 5 * 60 * 1000;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (transportManager == null) transportManager = new TransportManager(context);
        updateAppWidgets(context, appWidgetManager, appWidgetIds);
        planUpdates(context, appWidgetManager, appWidgetIds);
        setAlarm(context, appWidgetIds);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the associated setting from the database.
        for (int appWidgetId : appWidgetIds) {
            transportManager.deleteWidget(appWidgetId);
        }
        transportManager = null;
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        if (transportManager == null) transportManager = new TransportManager(context);
        // Enter relevant functionality for when the first widget is created
        setAlarm(context, this.getActiveWidgetIds(context));
    }

    @Override
    public void onDisabled(Context context) {
        // Cancel alarm as the last widget has been removed
        setAlarm(context, new int[0]);
        super.onDisabled(context);
    }

    /**
     * If no alarm is running yet a new alarm is started which repeats every minute
     */
    private static void setAlarm(Context context, int[] appWidgetIds) {
        boolean auto_reload = false;
        for (int appWidgetId : appWidgetIds) {
            if (transportManager.getWidget(appWidgetId).autoReload()) {
                auto_reload = true;
                break;
            }
        }
        Intent intent = new Intent(context, MVVWidget.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
        if (auto_reload) {
            intent.setAction(MVVWidget.BROADCAST_RELOAD_ALL);
            am.setRepeating(AlarmManager.RTC, UPDATE_ALARM_DELAY, UPDATE_ALARM_DELAY, sender);
        }
    }

    /**
     * Plans updates the widgets after 30s and 60s
     *
     * @param appWidgetIds the ids of the widgets to update
     */
    static void planUpdates(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        /*
        if (MVVWidget.timer == null) {
            MVVWidget.timer = new Timer();
        }
        MVVWidget.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateAppWidgets(context, appWidgetManager, appWidgetIds);
            }
        }, UPDATE_TRIGGER_DELAY);
        MVVWidget.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateAppWidgets(context, appWidgetManager, appWidgetIds);
            }
        }, 2 * UPDATE_TRIGGER_DELAY);
        */
    }

    /**
     * Updates the content of multiple widgets
     *
     * @param appWidgetIds the array of widget ids to update
     */
    static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            MVVWidget.updateAppWidget(context, appWidgetManager, widgetId, false);
        }
    }

    /**
     * Updates the content of the widget
     *
     * @param appWidgetId the id of the widget to update
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, boolean forceLoadData) {
        // Get the settings for this widget from the database
        TransportManager.WidgetDepartures widgetDepartures = transportManager.getWidget(appWidgetId);

        System.out.println("update" + appWidgetId + " force: " + forceLoadData + " " + widgetDepartures.getStationId());

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

        // Set up the reload functionality
        Intent reloadIntent = new Intent(context, MVVWidget.class);
        reloadIntent.setAction(MVVWidget.MVV_WIDGET_FORCE_RELOAD);
        reloadIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingReloadIntent = PendingIntent.getBroadcast(context, appWidgetId, reloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.mvv_widget_reload, pendingReloadIntent);
        rv.setViewVisibility(R.id.mvv_widget_reload, widgetDepartures.autoReload() ? View.GONE : View.VISIBLE);

        // Set up the intent that starts the MVVWidgetService, which will
        // provide the departure times for this station
        Intent intent = new Intent(context, MVVWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(MVV_WIDGET_FORCE_RELOAD, forceLoadData);
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
            case MVV_WIDGET_FORCE_RELOAD:
                int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                if (appWidgetId >= 0) {
                    updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, true);
                }
                break;
            case BROADCAST_RELOAD_ALL:
                System.out.println("BROADCAST RELOAD ALL");
                // There may be multiple widgets active, so update all of them
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                int[] appWidgetIds = getActiveWidgetIds(context);
                updateAppWidgets(context, appWidgetManager, this.getActiveWidgetIds(context));
                planUpdates(context, appWidgetManager, appWidgetIds);
                break;
        }
        super.onReceive(context, intent);
    }

    private int[] getActiveWidgetIds(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, MVVWidget.class);
        return appWidgetManager.getAppWidgetIds(thisWidget);
    }
}
