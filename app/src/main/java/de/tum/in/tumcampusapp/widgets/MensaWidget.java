package de.tum.in.tumcampusapp.widgets;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.managers.CafeteriaManager;
import de.tum.in.tumcampusapp.services.MensaWidgetService;


/**
 * Implementation of Mensa Widget functionality.
 * The Update intervals is set to 10 hours in mensa_widget_info.xml
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MensaWidget extends AppWidgetProvider {

    AppWidgetManager appWidgetManager;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        this.appWidgetManager = appWidgetManager;

        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, MensaWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.mensa_widget);

            // set the header for the Widget layout
            CafeteriaManager mensaManager = new CafeteriaManager(context);
            String mensaName = mensaManager.getBestMatchMensaName(context);
            rv.setTextViewText(R.id.mensa_widget_header, mensaName);

            // set the adapter for the list view in the mensaWidget
            rv.setRemoteAdapter(R.id.food_item, intent); //appWidgetIds[i],
            rv.setEmptyView(R.id.empty_view, R.id.empty_view);
            appWidgetManager.updateAppWidget(appWidgetId, rv);

        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

}


