package de.tum.`in`.tumcampusapp.component.ui.cafeteria.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.service.MensaWidgetService
import de.tum.`in`.tumcampusapp.utils.Const
import org.joda.time.format.DateTimeFormat

/**
 * Implementation of Mensa Widget functionality.
 * The Update intervals is set to 10 hours in mensa_widget_info.xml
 */
class MensaWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        val localRepository = CafeteriaLocalRepository(TcaDb.getInstance(context))
        val mensaManager = CafeteriaManager(context)

        val cafeteriaId = mensaManager.bestMatchMensaId
        val cafeteria = localRepository.getCafeteriaWithMenus(cafeteriaId)

        for (appWidgetId in appWidgetIds) {
            val remoteViews = RemoteViews(context.packageName, R.layout.mensa_widget)

            // Set the header for the Widget layout
            remoteViews.setTextViewText(R.id.mensa_widget_header, cafeteria.name)

            // Set the properly formatted date in the subhead
            val date = DateTimeFormat.shortDate().print(cafeteria.nextMenuDate)
            remoteViews.setTextViewText(R.id.mensa_widget_subhead, date)

            // Set the header on click to open the mensa activity
            val mensaIntent = Intent(context, CafeteriaActivity::class.java).apply {
                putExtra(Const.CAFETERIA_ID, mensaManager.bestMatchMensaId)
            }
            val pendingIntent = PendingIntent.getActivity(context, appWidgetId, mensaIntent, 0)
            remoteViews.setOnClickPendingIntent(R.id.mensa_widget_header_container, pendingIntent)

            // Set the adapter for the list view in the mensa widget
            val intent = Intent(context, MensaWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            remoteViews.setRemoteAdapter(R.id.food_item, intent)
            remoteViews.setEmptyView(R.id.empty_view, R.id.empty_view)

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }
}
