package de.tum.in.tumcampus.cards;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.TransportationDetailsActivity;
import de.tum.in.tumcampus.auxiliary.DepartureView;
import de.tum.in.tumcampus.models.managers.TransportManager;

import static de.tum.in.tumcampus.models.managers.CardManager.CARD_MVV;

/**
 * Card that shows MVV departure times
 */
public class MVVCard extends Card {
    private static final String MVV_TIME = "mvv_time";
    private String mStationName;
    private List<TransportManager.Departure> mDepartures;

    public MVVCard(Context context) {
        super(context, "card_mvv");
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new Card.CardViewHolder(view);
    }

    @Override
    public int getTyp() {
        return CARD_MVV;
    }

    @Override
    public String getTitle() {
        return mStationName;
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        mCard = viewHolder.itemView;
        mLinearLayout = (LinearLayout) mCard.findViewById(R.id.card_view);
        mTitleView = (TextView) mCard.findViewById(R.id.card_title);
        mTitleView.setText(getTitle());
        mCard.findViewById(R.id.place_holder).setVisibility(View.VISIBLE);

        //Remove old DepartureViews
        for(int i = 0; i < mLinearLayout.getChildCount(); i++) {
            if(mLinearLayout.getChildAt(i) instanceof DepartureView) {
                mLinearLayout.removeViewAt(i);
            }
        }

        for (int i = 0; i < mDepartures.size() && i < 5; i++) {
            TransportManager.Departure curr = mDepartures.get(i);

            DepartureView view = new DepartureView(mContext);
            view.setSymbol(curr.symbol);
            view.setLine(curr.line);
            view.setTime(curr.time);
            mLinearLayout.addView(view);
        }
    }

    @Override
    public Intent getIntent() {
        Intent i = new Intent(mContext, TransportationDetailsActivity.class);
        i.putExtra(TransportationDetailsActivity.EXTRA_STATION, mStationName);
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

        String firstContent = "", firstTime = "";
        for (TransportManager.Departure d : mDepartures) {
            if (firstTime.isEmpty()) {
                firstTime = d.time + "min";
                firstContent = d.symbol + " " + d.line;
            }

            NotificationCompat.Builder pageNotification =
                    new NotificationCompat.Builder(mContext)
                            .setContentTitle(d.time + "min")
                            .setContentText(d.symbol + " " + d.line);
            morePageNotification.addPage(pageNotification.build());
        }

        notificationBuilder.setContentTitle(firstTime);
        notificationBuilder.setContentText(firstContent);
        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.wear_mvv);
        morePageNotification.setBackground(bm);
        return morePageNotification.extend(notificationBuilder).build();
    }

    public void setStation(String station) {
        this.mStationName = station;
    }

    public void setDepartures(List<TransportManager.Departure> departures) {
        this.mDepartures = departures;
    }
}
