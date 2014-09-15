package de.tum.in.tumcampus.cards;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.TransportationDetailsActivity;
import de.tum.in.tumcampus.auxiliary.DepartureView;
import de.tum.in.tumcampus.models.managers.TransportManager;

import static de.tum.in.tumcampus.models.managers.CardManager.CARD_MVV;


public class MVVCard extends Card {
    private static final String MVV_TIME = "mvv_time";
    private String mStationName;
    private List<TransportManager.Departure> mDepartures;
    private Date mTime;

    public MVVCard(Context context) {
        super(context, "card_mvv");
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
    public View getCardView(Context context, ViewGroup parent) {
        super.getCardView(context, parent);
        mPlaceHolder.setVisibility(View.VISIBLE);
        for(TransportManager.Departure d : mDepartures)
            addDeparture(d.symbol, d.line,  d.time);
        return mCard;
    }

    private void addDeparture(String symbol, String title, long time) {
        DepartureView view = new DepartureView(mContext);
        view.setSymbol(symbol);
        view.setLine(title);
        view.setTime(mTime.getTime()+time*60000);
        mLinearLayout.addView(view);
    }

    @Override
    public Intent getIntent() {
        Intent i = new Intent(mContext, TransportationDetailsActivity.class);
        i.putExtra(TransportationDetailsActivity.EXTRA_STATION, mStationName);
        return i;
    }

    @Override
    protected void discard(Editor editor) {
        editor.putLong(MVV_TIME, mTime.getTime());
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        final long prevDate = prefs.getLong(MVV_TIME, 0);
        return prevDate+DateUtils.HOUR_IN_MILLIS < mTime.getTime();
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        NotificationCompat.WearableExtender morePageNotification =
                new NotificationCompat.WearableExtender();

        String firstContent = "", firstTime = "";
        for(TransportManager.Departure d : mDepartures) {
            if(firstTime.isEmpty()) {
                firstTime = d.time + "min";
                firstContent = d.symbol + " " + d.line;
            }

            NotificationCompat.Builder pageNotification =
                    new NotificationCompat.Builder(mContext)
                            .setContentTitle(d.time+"min")
                            .setContentText(d.symbol+" "+d.line);
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

    public void setTime(long time) {
        this.mTime = new Date(time);
    }
}
