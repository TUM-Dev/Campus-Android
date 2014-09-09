package de.tum.in.tumcampus.cards;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.TransportationDetailsActivity;
import de.tum.in.tumcampus.models.managers.TransportManager;

import static de.tum.in.tumcampus.models.managers.CardManager.CARD_MVV;


public class MVVCard extends Card {
    private static final String MVV_TIME = "mvv_time";
    private String mStationName;
    private Cursor mDepartures;
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
        if(mDepartures.moveToFirst()) {
            do {
                addDeparture(mDepartures.getString(0), mDepartures.getString(1),  mDepartures.getString(2));
            } while(mDepartures.moveToNext());
        }
        return mCard;
    }

    private void addDeparture(String symbol, String title, String time) {
        View view = mInflater.inflate(R.layout.card_departure_line, mLinearLayout, false);
        TextView symbolView = (TextView) view.findViewById(R.id.line_symbol);
        TextView textView = (TextView) view.findViewById(R.id.line_name);
        TextView timeView = (TextView) view.findViewById(R.id.line_time);
        TransportManager.setSymbol(mContext, symbolView, symbol);
        textView.setText(title);
        timeView.setText(time);
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
        if(mDepartures.moveToFirst()) {
            firstTime = mDepartures.getString(2);
            firstContent = mDepartures.getString(0)+" "+mDepartures.getString(1);
            do {
                NotificationCompat.Builder pageNotification =
                        new NotificationCompat.Builder(mContext)
                                .setContentTitle(mDepartures.getString(2))
                                .setContentText(mDepartures.getString(0)+" "+mDepartures.getString(1));
                morePageNotification.addPage(pageNotification.build());
            } while(mDepartures.moveToNext());
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

    public void setDepartures(Cursor departures) {
        this.mDepartures = departures;
    }

    public void setTime(long time) {
        this.mTime = new Date(time);
    }
}
