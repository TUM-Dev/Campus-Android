package de.tum.in.tumcampus.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.R;

public class CardsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        final int appID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        return new CardsRemoteViewsFactory(this.getApplicationContext(), appID);
    }
}

class CardsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    Context mContext;
    int appWidgetId;
    SharedPreferences prefs;
    List<RemoteViews> views = new ArrayList<>();

    CardsRemoteViewsFactory(Context context, int appWidgetId) {
        this.mContext = context;
        this.appWidgetId = appWidgetId;
        prefs = context.getSharedPreferences(CardsWidgetConfigureActivity.PREFS_NAME, 0);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        updateContent();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        return views.get(i);
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
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private void updateContent() {
        final String prefix = CardsWidgetConfigureActivity.PREF_PREFIX_KEY + appWidgetId;
        views.clear();
        CardManager.update(mContext);
        List<Card> cards = CardManager.getCards();
        for (Card card : cards) {
            final boolean getsShown = prefs.getBoolean(prefix + card.getTyp(), false);
            if (getsShown) {
                final RemoteViews remote = card.getRemoteViews(mContext);

                if (remote != null) {
                    //Set the intent to fill in
                    Intent fillInIntent = new Intent();
                    fillInIntent.putExtra("ID", cards.indexOf(card));
                    remote.setOnClickFillInIntent(R.id.cards_widget_card, fillInIntent);
                }

                views.add(remote);
            }
        }
    }
}
