package de.tum.in.tumcampus;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import java.util.List;
import java.util.Map;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.CafeteriaMenu;
import de.tum.in.tumcampus.models.managers.CafeteriaManager;
import de.tum.in.tumcampus.services.MensaWidgetService;


/**
 * Implementation of App Widget functionality.
 */
public class MensaWidget extends AppWidgetProvider {

    private RemoteViews rv;
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            Intent intent = new Intent(context, MensaWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            rv = new RemoteViews(context.getPackageName(), R.layout.mensa_widget);
            CafeteriaManager mensaManager = new CafeteriaManager(context);
            String mensaName = mensaManager.getBestMatchMensaName(context);
            rv.setTextViewText(R.id.mensa_widget_header, mensaName);
            rv.setRemoteAdapter(appWidgetIds[i], R.id.food_item, intent);
            rv.setEmptyView(R.id.empty_view, R.id.empty_view);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);

        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(final Context context, final Intent intent){
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        Utils.log("Receieved broadcast! " + intent.getAction());
        if (intent.getAction().equals(MensaWidgetService.ACTION_TELL_MENSA_NAME)){
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            String mensaName = intent.getStringExtra("mensa_name");
            Utils.log("got mensa name from broadcast !" + mensaName);
            rv.setTextViewText(R.id.mensa_widget_header, mensaName);
        }
        super.onReceive(context, intent);
    }
}


