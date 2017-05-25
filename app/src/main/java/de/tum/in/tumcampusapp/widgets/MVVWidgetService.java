package de.tum.in.tumcampusapp.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.MVVSymbolView;
import de.tum.in.tumcampusapp.managers.TransportManager;

@SuppressLint("Registered")
public class MVVWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MVVRemoteViewFactory(this.getApplicationContext(), intent);
    }

    private class MVVRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

        private final String station_id;
        private final Context applicationContext;
        private List<TransportManager.Departure> departures = new ArrayList<>();

        MVVRemoteViewFactory(Context applicationContext, Intent intent) {
            this.applicationContext = applicationContext.getApplicationContext();
            // Get the station from the Intent
            station_id = intent.getStringExtra(MVVWidget.EXTRA_STATION_ID);
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            if (station_id.length() > 0) {
                // load the departures for the station
                this.departures = TransportManager.getDeparturesFromExternal(this.applicationContext, station_id);
            }
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            if (departures == null) {
                return 0;
            }
            return departures.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            // get the departure for this view
            TransportManager.Departure currentItem = departures.get(position);
            if (currentItem == null) {
                return null;
            }

            RemoteViews rv = new RemoteViews(applicationContext.getPackageName(), R.layout.departure_line_widget);

            // Setup the line symbol
            rv.setTextViewText(R.id.line_symbol, currentItem.symbol);
            MVVSymbolView d = new MVVSymbolView(currentItem.symbol);
            rv.setTextColor(R.id.line_symbol, d.getTextColor());
            rv.setInt(R.id.line_symbol, "setBackgroundColor", d.getBackgroundColor());

            // Setup the line name and the departure time
            rv.setTextViewText(R.id.line_name, currentItem.direction);
            rv.setTextViewText(R.id.departure_time, currentItem.countDown + " min");

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

}