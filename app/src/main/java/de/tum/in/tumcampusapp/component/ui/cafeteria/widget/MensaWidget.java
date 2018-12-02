package de.tum.in.tumcampusapp.component.ui.cafeteria.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity;
import de.tum.in.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.Cafeteria;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.service.MensaWidgetService;
import de.tum.in.tumcampusapp.utils.Const;

/**
 * Implementation of Mensa Widget functionality.
 * The Update intervals is set to 10 hours in mensa_widget_info.xml
 */
public class MensaWidget extends AppWidgetProvider {

    private CafeteriaLocalRepository localRepository;
    private CafeteriaManager mensaManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        localRepository = new CafeteriaLocalRepository(TcaDb.getInstance(context));
        mensaManager = new CafeteriaManager(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        int cafeteriaId = mensaManager.getBestMatchMensaId();
        Cafeteria cafeteria = localRepository.getCafeteria(cafeteriaId);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.mensa_widget);

            // TODO: Investigate how this can be null
            // Set the header for the Widget layout
            if (cafeteria != null) {
                rv.setTextViewText(R.id.mensa_widget_header, cafeteria.getName());
            }

            // Set the properly formatted date in the subhead
            LocalDate localDate = DateTime.now().toLocalDate();
            String date = DateTimeFormat.shortDate().print(localDate);
            rv.setTextViewText(R.id.mensa_widget_subhead, date);

            // Set the header on click to open the mensa activity
            Intent mensaIntent = new Intent(context, CafeteriaActivity.class);
            mensaIntent.putExtra(Const.CAFETERIA_ID, mensaManager.getBestMatchMensaId());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, mensaIntent, 0);
            rv.setOnClickPendingIntent(R.id.mensa_widget_header_container, pendingIntent);

            // Set the adapter for the list view in the mensa widget
            Intent intent = new Intent(context, MensaWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            rv.setRemoteAdapter(R.id.food_item, intent);
            rv.setEmptyView(R.id.empty_view, R.id.empty_view);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }

}
