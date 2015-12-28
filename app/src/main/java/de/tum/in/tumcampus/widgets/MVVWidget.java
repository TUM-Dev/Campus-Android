package de.tum.in.tumcampus.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import java.util.Calendar;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.services.MVVWidgetService;

/**
 * AppWidgetProvider for the MVV widget
 */
public class MVVWidget extends AppWidgetProvider {

    private RemoteViews rv;
    AppWidgetManager appWidgetManager;
    int[] widgetIDs = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        widgetIDs = appWidgetIds;
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        this.appWidgetManager = appWidgetManager;
        for (int i = 0; i < N; i++) {
            Intent intent = new Intent(context, MVVWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            rv = new RemoteViews(context.getPackageName(), R.layout.mvv_widget);

            // set the header for the Widget layout
            Calendar c = Calendar.getInstance();
            String headerMVVWidget = "MVV Recent Searches " + c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.YEAR);
            rv.setTextViewText(R.id.mvv_widget_header, headerMVVWidget);

            // set the adapter for the list view in the mensaWidget
            rv.setRemoteAdapter(R.id.mvv_widget_item, intent);//appWidgetIds[i],
            rv.setEmptyView(R.id.empty_view, R.id.empty_view);
            //Set pending intent to use the refresh button
            rv.setOnClickPendingIntent(R.id.mvv_refresh, getPendingSelfIntent(context, Const.SYNC_CLICKED));

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);

        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * Method reacting to the click on the refresh button
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        //if click on the refresh button
        if (Const.SYNC_CLICKED.equals(intent.getAction())) {
            //Update widget
            if (!MVVWidgetService.loadRecentData())
                Utils.showToast(context, "No recent searches to show");
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }


}


