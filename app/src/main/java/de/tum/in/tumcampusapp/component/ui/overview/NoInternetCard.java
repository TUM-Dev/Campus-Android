package de.tum.in.tumcampusapp.component.ui.overview;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.overview.card.StickyCard;
import de.tum.in.tumcampusapp.service.DownloadWorker;
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
    public void updateViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);

        View v = viewHolder.itemView;
        TextView lastUpdate = v.findViewById(R.id.card_last_update);
        ReadableInstant lastUpdated = new DateTime(DownloadWorker.lastUpdate(getContext()));
        final String time = DateUtils.getRelativeTimeSpanString(lastUpdated.getMillis(),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
                .toString();
        lastUpdate.setText(String.format(getContext().getString(R.string.last_updated), time));
    }

    @Override
    protected boolean shouldShow(@NonNull SharedPreferences prefs) {
        return !NetUtils.isConnected(getContext());
    }

    @Override
    public int getId() {
        return 0;
    }
}
