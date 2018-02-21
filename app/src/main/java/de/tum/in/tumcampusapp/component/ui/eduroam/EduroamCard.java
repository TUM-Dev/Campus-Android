package de.tum.in.tumcampusapp.component.ui.eduroam;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.NotificationAwareCard;

/**
 * Card that can start {@link SetupEduroamActivity}
 */
public class EduroamCard extends NotificationAwareCard {

    public EduroamCard(Context context) {
        super(CardManager.CARD_EDUROAM, context, "card_eduroam", false, true);
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_eduroam, parent, false);
        return new Card.CardViewHolder(view);
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        // NOOP
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        //Check if wifi is turned on at all, as we cannot say if it was configured if its off
        WifiManager wifi = (WifiManager) mContext.getApplicationContext()
                                                 .getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            return false;
        }

        return EduroamController.getEduroamConfig(mContext) == null;
    }

    @Override
    protected void discard(SharedPreferences.Editor editor) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit()
             .putBoolean("card_eduroam_start", false)
             .apply();
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        return null;
    }

    @Override
    public String getTitle() {
        return mContext.getString(R.string.setup_eduroam);
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, SetupEduroamActivity.class);
    }

    @Override
    public int getId() {
        return 5000;
    }

    @Override
    public RemoteViews getRemoteViews(Context context) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.cards_widget_card);
        remoteViews.setTextViewText(R.id.widgetCardTextView, this.getTitle());
        remoteViews.setImageViewResource(R.id.widgetCardImageView, R.drawable.ic_action_network_wifi);
        return remoteViews;
    }
}
