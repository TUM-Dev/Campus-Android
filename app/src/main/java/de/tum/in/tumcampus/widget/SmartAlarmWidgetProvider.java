package de.tum.in.tumcampus.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.SmartAlarmInfo;
import de.tum.in.tumcampus.services.SmartAlarmReceiver;

public class SmartAlarmWidgetProvider extends AppWidgetProvider {
    public static final String ACTIVATING = "SMART_ALARM_ACTIVATING";

    public static String ACTION_UPDATE_WIDGET = "de.tum.in.tumcampus.widget.SmartAlarmWidgetProvider.action.UPDATE_WIDGET";

    private static SmartAlarmInfo sai;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateWidget(context, false);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        if (!intent.getAction().equals(ACTION_UPDATE_WIDGET)) {
            super.onReceive(context, intent);
            return;
        }

        if (intent.getExtras().get(SmartAlarmReceiver.INFO) != null) {
            sai = (SmartAlarmInfo) intent.getExtras().get(SmartAlarmReceiver.INFO);
        }

        updateWidget(context, intent.getBooleanExtra(ACTIVATING, false));
    }

    /**
     * Updates info shown on the widget
     *
     * @param context    the current context
     * @param activating is the alarm activating
     */
    private void updateWidget(Context context, boolean activating) {
        Utils.log("SmartAlarm: update widget");
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.smart_alarm_widget);

        if (activating) {
            showActivatingWidget(context, rv);
        } else if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("smart_alarm_active", false) && sai != null) {
            showActiveWidget(context, rv);
        } else {
            showInactiveWidget(context, rv);
        }

        AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, SmartAlarmWidgetProvider.class), rv);
    }

    /**
     * Show widget in active status
     *
     * @param context Context
     * @param rv      RemoteView of the widgets
     */
    private void showActiveWidget(Context context, RemoteViews rv) {
        rv.setTextViewText(R.id.alarm_date, sai.getFormattedWakeupDate(context));
        rv.setFloat(R.id.alarm_date, "setTextSize", 18);

        rv.setTextViewText(R.id.alarm_time, sai.getFormattedWakeupTime(context));
        rv.setTextViewText(R.id.next_lecture_title, sai.getLectureTitle());

        if (sai.getFirstTransportType() != SmartAlarmInfo.TransportType.PRIVATE) {
            if (sai.getFirstTransportType() == SmartAlarmInfo.TransportType.FOOT) {
                String walkTo = context.getString(R.string.smart_alarm_walk_to);
                rv.setTextViewText(R.id.transport_destination, walkTo + " " + sai.getFirstTrainDst());
            } else {
                rv.setImageViewResource(R.id.transport_icon, sai.getFirstTransportType().getIcon());
                rv.setTextViewText(R.id.transport_nr, sai.getFirstTrainLabel());
                rv.setTextViewText(R.id.transport_destination, sai.getFirstTrainDst());
            }

            rv.setTextViewText(R.id.transport_departure, sai.getFormattedDeparture(context));
        } else {
            rv.setTextViewText(R.id.transport_destination, "");
            rv.setTextViewText(R.id.transport_departure, "");
            rv.setTextViewText(R.id.transport_nr, "");
            rv.setImageViewResource(R.id.transport_icon, android.R.color.transparent);
        }

        Intent i = new Intent(context, SmartAlarmReceiver.class);
        i.setAction(SmartAlarmReceiver.ACTION_TOGGLE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.clock_icon, pi);
    }

    /**
     * Shows widget in waiting status (during calculations)
     *
     * @param context Context
     * @param rv      RemoteView of the widgets
     */
    private void showActivatingWidget(Context context, RemoteViews rv) {
        showInactiveWidget(context, rv);
        rv.setTextViewText(R.id.alarm_date, context.getString(R.string.smart_alarm_activating));
        rv.setOnClickPendingIntent(R.id.clock_icon, null);
    }

    /**
     * Shows widget in inactive status
     *
     * @param context Context
     * @param rv      RemoteView of the widgets
     */
    private void showInactiveWidget(Context context, RemoteViews rv) {
        rv.setTextViewText(R.id.alarm_date, context.getString(R.string.smart_alarm_activate));
        rv.setFloat(R.id.alarm_date, "setTextSize", 16);

        rv.setTextViewText(R.id.next_lecture_title, context.getString(R.string.smart_alarm_inactive));

        rv.setTextViewText(R.id.alarm_time, "");
        rv.setTextViewText(R.id.transport_destination, "");
        rv.setTextViewText(R.id.transport_departure, "");
        rv.setTextViewText(R.id.transport_nr, "");
        rv.setImageViewResource(R.id.transport_icon, android.R.color.transparent);

        Intent i = new Intent(context, SmartAlarmReceiver.class);
        i.setAction(SmartAlarmReceiver.ACTION_TOGGLE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.clock_icon, pi);
    }
}
