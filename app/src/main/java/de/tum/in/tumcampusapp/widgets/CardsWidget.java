package de.tum.in.tumcampusapp.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import java.net.URISyntaxException;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link CardsWidgetConfigureActivity CardsWidgetConfigureActivity}
 */
public class CardsWidget extends AppWidgetProvider {

    private static final String BROADCAST_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_CARDSWIDGET";
    static final String TARGET_INTENT = "TARGET_INTENT";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            CardsWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Set up the intent that starts the StackViewService, which will
        // provide the views for this collection.
        Intent intent = new Intent(context, CardsWidgetService.class);
        // Add the app widget ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        // Instantiate the RemoteViews object for the app widget layout.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.cards_widget);
        // Set up the RemoteViews object to use a RemoteViews adapter.
        // This adapter connects to a RemoteViewsService  through the specified intent.
        // This is how you populate the data.
        rv.setRemoteAdapter(R.id.card_widget_listview, intent);

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        rv.setEmptyView(R.id.card_widget_listview, R.layout.cards_widget_card);

        //Set the pendingIntent Template
        Intent broadcastIntent = new Intent(context, CardsWidget.class);
        broadcastIntent.setAction(BROADCAST_NAME);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.card_widget_listview, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        if (intent.getAction()
                  .equals(BROADCAST_NAME)) {
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
        }
        super.onReceive(context, intent);
    }
}

