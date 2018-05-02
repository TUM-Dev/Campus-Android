package de.tum.in.tumcampusapp.component.ui.overview;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.overview.card.StickyCard;
import de.tum.in.tumcampusapp.service.DownloadService;
import de.tum.in.tumcampusapp.utils.NetUtils;

/**
 * Card that informs that no internet connection is available
 */
public class NoInternetCard extends StickyCard {

    public NoInternetCard(Context context) {
        super(CardManager.CARD_NO_INTERNET, context);
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_no_internet, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);

        View v = viewHolder.itemView;
        TextView lastUpdate = v.findViewById(R.id.card_last_update);
        Date lastUpdated = new Date(DownloadService.Companion.lastUpdate(getContext()));
        final String time = DateUtils.getRelativeTimeSpanString(lastUpdated.getTime(),
                                                                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
                                     .toString();
        lastUpdate.setText(String.format(getContext().getString(R.string.last_updated), time));
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        return !NetUtils.isConnected(getContext());
    }

    @Override
    public Intent getIntent() {
        return null;
    }

    @Override
    public int getId() {
        return 0;
    }
}
