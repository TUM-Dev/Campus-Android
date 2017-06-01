package de.tum.in.tumcampusapp.widgets;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;

@SuppressLint("Registered")
public class TimetableWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TimetableRemoteViewFactory(this.getApplicationContext(), intent);
    }

    private class TimetableRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

        private final Context applicationContext;
        private int appWidgetID;

        TimetableRemoteViewFactory(Context applicationContext, Intent intent) {
            this.applicationContext = applicationContext.getApplicationContext();
            this.appWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            /*
            RemoteViews rv = new RemoteViews(applicationContext.getPackageName(), R.layout.departure_line_widget);
            if (this.departures == null) {
                return rv;
            }

            // get the departure for this view
            TransportManager.Departure currentItem = this.departures.get(position);
            if (currentItem == null) {
                return null;
            }

            // Setup the line symbol
            rv.setTextViewText(R.id.line_symbol, currentItem.symbol);
            MVVSymbolView d = new MVVSymbolView(currentItem.symbol);
            rv.setTextColor(R.id.line_symbol, d.getTextColor());
            rv.setInt(R.id.line_symbol, "setBackgroundColor", d.getBackgroundColor());

            // Setup the line name and the departure time
            rv.setTextViewText(R.id.line_name, currentItem.direction);
            rv.setTextViewText(R.id.departure_time, currentItem.getCalculatedCountDown()" min");

            return rv;
            */
            return null;
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