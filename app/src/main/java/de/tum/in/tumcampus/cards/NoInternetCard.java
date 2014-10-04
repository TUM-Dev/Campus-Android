package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.services.DownloadService;

/**
 * Card that informs that no internet connection is available
 */
public class NoInternetCard extends Card {

    public NoInternetCard(Context context) {
        super(context);
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_NO_INTERNET;
    }

    @Override
    public View getCardView(Context context, ViewGroup parent) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.card_no_internet, parent, false);
        TextView lastUpdate = (TextView) v.findViewById(R.id.card_last_update);
        Date lastUpdated = new Date(DownloadService.lastUpdate(context));
        final String time = DateUtils.getRelativeTimeSpanString(lastUpdated.getTime(),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        lastUpdate.setText(String.format(context.getString(R.string.last_updated),time));
        return v;
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        return !NetUtils.isConnected(mContext);
    }

    @Override
    public boolean isDismissable() {
        return false;
    }
}
