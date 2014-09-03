package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.CafeteriaDetailsActivity;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.fragments.CafeteriaDetailsSectionFragment;
import de.tum.in.tumcampus.models.CafeteriaMenu;
import de.tum.in.tumcampus.models.managers.TransportManager;

import static de.tum.in.tumcampus.models.managers.CardManager.CARD_MVV;


public class MVVCard extends Card {
    private static final String MVV_TIME = "mvv_time";
    private String mStationName;
    private Cursor mDepartures;
    private Date mTime;

    public MVVCard(Context context) {
        super(context, "card_mvv_setting");
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
        ImageView symbolView = (ImageView) view.findViewById(R.id.line_symbol);
        TextView textView = (TextView) view.findViewById(R.id.line_name);
        TextView timeView = (TextView) view.findViewById(R.id.line_time);
        TransportManager.setSymbol(mContext, symbolView, symbol);
        textView.setText(title);
        timeView.setText(time);
        mLinearLayout.addView(view);
    }

    /*
    @Override
    public Intent getIntent() {
        Intent i = new Intent(mContext, CafeteriaDetailsActivity.class);
        i.putExtra(Const.CAFETERIA_ID, mCafeteriaId);
        i.putExtra(Const.CAFETERIA_NAME, mCafeteriaName);
        return i;
    }*/

    @Override
    protected void discard(Editor editor) {
        editor.putLong(MVV_TIME, mTime.getTime());
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        final long prevDate = prefs.getLong(MVV_TIME, 0);
        return prevDate+DateUtils.HOUR_IN_MILLIS < mTime.getTime();
    }
/*
    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        HashMap<String, String> rolePrices = CafeteriaDetailsSectionFragment.getRolePrices(mContext);

        NotificationCompat.WearableExtender morePageNotification =
                new NotificationCompat.WearableExtender();

        String allContent = "", firstContent = "";
        for (CafeteriaMenu menu : mMenus) {
            if (menu.typeShort.equals("bei"))
                continue;

            NotificationCompat.Builder pageNotification =
                    new NotificationCompat.Builder(mContext)
                            .setContentTitle(menu.typeLong);

            String content = menu.name;
            if (rolePrices.containsKey(menu.typeLong))
                content +=  "\n"+rolePrices.get(menu.typeLong) + " €";

            content = content.replaceAll("\\([^\\)]+\\)", "").trim();
            pageNotification.setContentText(content);
            if(menu.typeShort.equals("tg")) {
                if(!allContent.isEmpty())
                    allContent += "\n";
                allContent += content;
            }
            if(firstContent.isEmpty()) {
                firstContent =  menu.name.replaceAll("\\([^\\)]+\\)", "").trim()+"...";
            }

            morePageNotification.addPage(pageNotification.build());
        }

        notificationBuilder.setWhen(mDate.getTime());
        notificationBuilder.setContentText(firstContent);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(allContent));
        return morePageNotification.extend(notificationBuilder).build();
    }*/

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
