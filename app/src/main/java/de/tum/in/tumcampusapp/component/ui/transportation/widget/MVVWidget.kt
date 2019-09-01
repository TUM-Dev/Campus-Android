package de.tum.`in`.tumcampusapp.component.ui.transportation.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController
import org.jetbrains.anko.alarmManager
import java.util.*

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [MVVWidgetConfigureActivity]
 */
class MVVWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAppWidgets(context, appWidgetManager, appWidgetIds)
        setAlarm(context)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the associated setting from the database.
        for (appWidgetId in appWidgetIds) {
            TransportController(context).deleteWidget(appWidgetId)
        }
        super.onDeleted(context, appWidgetIds)
    }

    override fun onDisabled(context: Context) {
        // Cancel alarm as the last widget has been removed
        setAlarm(context)
        super.onDisabled(context)
    }

    /**
     * If no alarm is running yet a new alarm is started which repeats every minute
     */
    private fun setAlarm(context: Context) {
        var autoReload = false

        for (appWidgetId in getActiveWidgetIds(context)) {
            val widgetDepartures = TransportController(context).getWidget(appWidgetId)
            if (widgetDepartures.autoReload) {
                autoReload = true
                break
            }
        }

        val intent = Intent(context, MVVWidget::class.java)
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager = context.alarmManager
        alarmManager.cancel(sender)
        if (autoReload) {
            intent.action = BROADCAST_RELOAD_ALL_ALARM
            alarmManager.setRepeating(AlarmManager.RTC, 5000, UPDATE_ALARM_DELAY.toLong(), sender)
        }
    }

    /**
     * Updates the widget every 20 seconds
     */
    private fun planUpdates(context: Context) {
        for (i in 1..3) {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    val reloadIntent = Intent(context, MVVWidget::class.java)
                    reloadIntent.action = BROADCAST_RELOAD_ALL
                    context.sendBroadcast(reloadIntent)
                }
            }, (UPDATE_TRIGGER_DELAY * i).toLong())
        }
    }

    /**
     * Updates the content of multiple widgets
     *
     * @param appWidgetIds the array of widget ids to update
     */
    private fun updateAppWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId, false)
        }
    }

    /**
     * Updates the content of the widget
     *
     * @param appWidgetId the id of the widget to update
     */
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        forceLoadData: Boolean
    ) {
        // Get the settings for this widget from the database
        val widgetDepartures = TransportController(context).getWidget(appWidgetId)

        // Instantiate the RemoteViews object for the app widget layout.
        val remoteViews = RemoteViews(context.packageName, R.layout.mvv_widget)
        remoteViews.setTextViewText(R.id.mvv_widget_station, widgetDepartures.station)

        // Set up the configuration activity listeners
        val configIntent = Intent(context, MVVWidgetConfigureActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.mvv_widget_setting_button, pendingIntent)

        // Set up the reload functionality
        val reloadIntent = Intent(context, MVVWidget::class.java).apply {
            action = MVV_WIDGET_FORCE_RELOAD
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val pendingReloadIntent = PendingIntent.getBroadcast(
                context, appWidgetId, reloadIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.mvv_widget_reload_button, pendingReloadIntent)

        val isAutoReload = widgetDepartures.autoReload
        remoteViews.setViewVisibility(R.id.mvv_widget_reload_button, if (isAutoReload) View.GONE else View.VISIBLE)

        // Set up the intent that starts the MVVWidgetService, which will
        // provide the departure times for this station
        val intent = Intent(context, MVVWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(MVV_WIDGET_FORCE_RELOAD, forceLoadData)
        }
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
        remoteViews.setRemoteAdapter(R.id.mvv_widget_listview, intent)

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        remoteViews.setEmptyView(R.id.mvv_widget_listview, R.id.empty_list_item)

        // Instruct the widget manager to update the widget
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.mvv_widget_listview)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            null, BROADCAST_RELOAD_ALL -> updateAppWidgets(context, AppWidgetManager.getInstance(context), getActiveWidgetIds(context))
            BROADCAST_RELOAD_ALL_ALARM -> {
                planUpdates(context)
                updateAppWidgets(context, AppWidgetManager.getInstance(context), getActiveWidgetIds(context))
            }
            MVV_WIDGET_FORCE_RELOAD -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                if (appWidgetId >= 0) {
                    updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, true)
                }
            }
        }
        super.onReceive(context, intent)
    }

    companion object {

        private const val BROADCAST_RELOAD_ALL_ALARM = "de.tum.in.newtumcampus.intent.action.BROADCAST_MVV_WIDGET_RELOAD_ALL_ALARM"
        private const val BROADCAST_RELOAD_ALL = "de.tum.in.newtumcampus.intent.action.BROADCAST_MVV_WIDGET_RELOAD_ALL"
        internal const val MVV_WIDGET_FORCE_RELOAD = "de.tum.in.newtumcampus.intent.action.MVV_WIDGET_FORCE_RELOAD"

        const val UPDATE_ALARM_DELAY = 60 * 1000
        const val UPDATE_TRIGGER_DELAY = 20 * 1000
        const val DOWNLOAD_DELAY = 5 * 60 * 1000

        private val timer = Timer()

        private fun getActiveWidgetIds(context: Context): IntArray {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MVVWidget::class.java)
            return appWidgetManager.getAppWidgetIds(thisWidget)
        }
    }
}
