package de.tum.in.tumcampusapp.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

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

        private final Context applicationContext;
        private List<TransportManager.Departure> departures;

        MVVRemoteViewFactory(Context applicationContext, Intent intent) {
            this.applicationContext = applicationContext.getApplicationContext();
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            this.departures = TransportManager.getDeparturesFromExternal(this.applicationContext, "München, Freimann");
            System.out.println("loaded departures");
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
            TransportManager.Departure currentItem = departures.get(position);
            if (currentItem == null) {
                return null;
            }

            RemoteViews rv = new RemoteViews(applicationContext.getPackageName(), R.layout.departure_line_widget);
            rv.setTextViewText(R.id.line_symbol, currentItem.symbol);

            MVVSymbolView d = new MVVSymbolView(currentItem.symbol);
            rv.setTextColor(R.id.line_symbol, d.getTextColor());
            rv.setInt(R.id.line_symbol, "setBackgroundColor", d.getBackgroundColor());

            rv.setTextViewText(R.id.line_name, currentItem.direction);
            rv.setTextViewText(R.id.line_switcher, currentItem.countDown + " min");

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