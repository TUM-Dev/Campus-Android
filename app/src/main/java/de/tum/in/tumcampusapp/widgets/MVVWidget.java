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
import android.widget.RemoteViews;
import de.tum.in.tumcampusapp.R;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MVVWidgetConfigureActivity MVVWidgetConfigureActivity}
 */
public class MVVWidget extends AppWidgetProvider {

    private static final String BROADCAST_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_MVVWIDGET";
    private static final String BROADCAST_ALARM_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_MVVWIDGET_ALARM";
    static final String TARGET_INTENT = "TARGET_INTENT";
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
        // When the user deletes the widget, delete the preference associated with it.
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        setAlarm(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Intent intent = new Intent(context, MVVWidget.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        alarmIsSet = false;
        super.onDisabled(context);
    }

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

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        System.out.println("update MVVWidget " + appWidgetId);

        // Set up the intent that starts the StackViewService, which will
        // provide the views for this collection.
        Intent intent = new Intent(context, MVVWidgetService.class);
        // Add the app widget ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        // Instantiate the RemoteViews object for the app widget layout.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.mvv_widget);
        // Set up the RemoteViews object to use a RemoteViews adapter.
        // This adapter connects to a RemoteViewsService  through the specified intent.
        // This is how you populate the data.
        rv.setRemoteAdapter(R.id.mvv_widget_listview, intent);

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        rv.setEmptyView(R.id.mvv_widget_listview, R.layout.departure_line_widget);

        //Set the pendingIntent Template
        //Intent broadcastIntent = new Intent(context, CardsWidget.class);
        //broadcastIntent.setAction(BROADCAST_NAME);
        //PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //rv.setPendingIntentTemplate(R.id.card_widget_listview, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, rv);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.mvv_widget_listview);

    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        switch (intent.getAction()) {
            case BROADCAST_NAME:
                /*
                String targetIntent = intent.getStringExtra(TARGET_INTENT);
                if (targetIntent != null) {
                    try {
                        //We try to recreate the targeted Intent from card.getIntent()
                        //CardsRemoteViewsFactory filled into this Broadcast
                        final Intent i = Intent.parseUri(targetIntent, Intent.URI_INTENT_SCHEME);
                        final Bundle extras = intent.getExtras();
                        extras.remove(TARGET_INTENT);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtras(extras);
                        context.startActivity(i);
                    } catch (URISyntaxException e) {
                        Utils.log(e);
                    }
                }
                */
                break;
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
