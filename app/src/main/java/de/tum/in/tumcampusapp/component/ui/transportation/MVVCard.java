package de.tum.in.tumcampusapp.component.ui.transportation;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.overview.card.NotificationAwareCard;
import de.tum.in.tumcampusapp.component.ui.transportation.model.efa.Departure;
import de.tum.in.tumcampusapp.component.ui.transportation.model.efa.StationResult;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

import static de.tum.in.tumcampusapp.component.ui.overview.CardManager.CARD_MVV;

/**
 * Card that shows MVV departure times
 */
public class MVVCard extends NotificationAwareCard {

    private static final String MVV_TIME = "mvv_time";

    private StationResult mStationResult;
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

    @Override
    public String getTitle() {
        return mStationResult.getStation();
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);

        if (viewHolder instanceof MVVCardViewHolder) {
            MVVCardViewHolder holder = (MVVCardViewHolder) viewHolder;
            holder.bind(mStationResult, mDepartures);
        }
    }

    @Override
    public Intent getIntent() {
        Intent i = new Intent(getContext(), TransportationDetailsActivity.class);
        i.putExtra(TransportationDetailsActivity.EXTRA_STATION, mStationResult.getStation());
        i.putExtra(TransportationDetailsActivity.EXTRA_STATION_ID, mStationResult.getId());
        return i;
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

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        NotificationCompat.WearableExtender morePageNotification = new NotificationCompat.WearableExtender();

        String firstContent = "";
        String firstTime = "";
        for (Departure d : mDepartures) {
            if (firstTime.isEmpty()) {
                firstTime = d.getCountDown() + "min";
                firstContent = d.getServingLine() + " " + d.getDirection();
            }

            NotificationCompat.Builder pageNotification =
                    new NotificationCompat.Builder(getContext(), Const.NOTIFICATION_CHANNEL_MVV)
                            .setContentTitle(d.getCountDown() + "min")
                            .setSmallIcon(R.drawable.ic_notification)
                            .setLargeIcon(Utils.getLargeIcon(getContext(), R.drawable.ic_mvv))
                            .setContentText(d.getServingLine() + " " + d.getDirection());
            morePageNotification.addPage(pageNotification.build());
        }

        notificationBuilder.setContentTitle(firstTime);
        notificationBuilder.setContentText(firstContent);
        Bitmap bm = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.wear_mvv);
        morePageNotification.setBackground(bm);
        return morePageNotification.extend(notificationBuilder)
                                   .build();
    }

    public void setStation(StationResult station) {
        mStationResult = station;
    }

    public void setDepartures(List<Departure> departures) {
        this.mDepartures = departures;
    }

    @Override
    public RemoteViews getRemoteViews(Context context, int appWidgetId) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.cards_widget_card);
        remoteViews.setTextViewText(R.id.widgetCardTextView, this.getTitle());
        remoteViews.setImageViewResource(R.id.widgetCardImageView, R.drawable.ic_mvv);
        return remoteViews;
    }
}
