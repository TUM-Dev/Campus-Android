package de.tum.in.tumcampus.cards;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.SetupEduroamActivity;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.EduroamManager;

/**
 * Card that can start {@link de.tum.in.tumcampus.activities.SetupEduroamActivity}
 */
public class EduroamCard extends Card {

    public EduroamCard(Context context) {
        super(context, "card_eduroam", false, true);
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_eduroam, parent, false);
        return new Card.CardViewHolder(view);
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_EDUROAM;
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {

    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        EduroamManager manager = new EduroamManager(mContext);
        return !manager.isConfigured();
    }

    @Override
    protected void discard(SharedPreferences.Editor editor) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit().putBoolean("card_eduroam_start", false).apply();
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        return null;
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, SetupEduroamActivity.class);
    }
}
