package de.tum.in.tumcampus.widgets;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import de.tum.in.tumcampus.models.managers.CardManager;

public class CardsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CardsRemoteViewsFactory(this.getApplicationContext());
    }
}

class CardsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    Context mContext;

    CardsRemoteViewsFactory(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        CardManager.update(mContext);
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return CardManager.getCardCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        return CardManager.getCard(i).getRemoteViews(mContext);
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
}
