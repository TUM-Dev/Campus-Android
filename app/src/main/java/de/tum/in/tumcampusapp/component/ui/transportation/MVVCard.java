package de.tum.in.tumcampusapp.component.ui.transportation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.transportation.model.efa.Departure;
import de.tum.in.tumcampusapp.component.ui.transportation.model.efa.StationResult;

import static de.tum.in.tumcampusapp.component.ui.overview.CardManager.CARD_MVV;

/**
 * Card that shows MVV departure times
 */
public class MVVCard extends Card {

    private static final String MVV_TIME = "mvv_time";

    private StationResult mStation;
    private List<Departure> mDepartures;

    MVVCard(Context context) {
        super(CARD_MVV, context, "card_mvv");
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.card_mvv, parent, false);
        return new MVVCardViewHolder(view);
    }

    public String getTitle() {
        return mStation.getStation();
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);

        if (viewHolder instanceof MVVCardViewHolder) {
            MVVCardViewHolder holder = (MVVCardViewHolder) viewHolder;
            holder.bind(mStation, mDepartures);
        }
    }

    @Nullable
    @Override
    public Intent getIntent() {
        return mStation.getIntent(getContext());
    }

    @Override
    protected void discard(Editor editor) {
        editor.putLong(MVV_TIME, System.currentTimeMillis());
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        final long prevDate = prefs.getLong(MVV_TIME, 0);
        return prevDate + DateUtils.HOUR_IN_MILLIS < System.currentTimeMillis();
    }
    
    public void setStation(StationResult station) {
        this.mStation = station;
    }

    public void setDepartures(List<Departure> departures) {
        this.mDepartures = departures;
    }

}
