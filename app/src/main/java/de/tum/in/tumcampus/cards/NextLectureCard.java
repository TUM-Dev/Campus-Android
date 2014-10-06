package de.tum.in.tumcampus.cards;

import android.app.Notification;
import android.app.SearchManager;
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
import android.widget.TextView;

import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.CalendarActivity;
import de.tum.in.tumcampus.activities.RoomFinderActivity;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CardManager;


public class NextLectureCard extends Card {

    private static final String NEXT_LECTURE_DATE = "next_date";
    private static final String NEXT_LECTURE_TITLE = "next_title";
    private String mTitle;
    private Date mDate;
    private String mLocation;

    public NextLectureCard(Context context) {
        super(context, "card_next_lecture");
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_NEXT_LECTURE;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public View getCardView(Context context, ViewGroup parent) {
        super.getCardView(context, parent);

        //Add content
        final String time = DateUtils.getRelativeDateTimeString(mContext, mDate.getTime(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
        addTextView(time);

        //Add location with link to room finder
        if (mLocation != null) {
            TextView location = addTextView(mContext.getString(R.string.room) + ": " + mLocation);
            location.setTextColor(mContext.getResources().getColor(R.color.holo_blue_bright));
            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, RoomFinderActivity.class);
                    i.setAction(Intent.ACTION_SEARCH);
                    i.putExtra(SearchManager.QUERY, mLocation);
                    mContext.startActivity(i);
                }
            });
        }
        return mCard;
    }

    @Override
    protected void discard(Editor editor) {
        editor.putLong(NEXT_LECTURE_DATE, mDate.getTime());
        editor.putString(NEXT_LECTURE_TITLE, mTitle);
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        long prevTime = prefs.getLong(NEXT_LECTURE_DATE, 0);
        String prevTitle = prefs.getString(NEXT_LECTURE_TITLE, "");
        return (mDate.getTime() == prevTime && !prevTitle.equals(mTitle)) || mDate.getTime() > prevTime;
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        final String time = DateUtils.getRelativeDateTimeString(mContext, mDate.getTime(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
        notificationBuilder.setContentText(mTitle + "\n" + time);
        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.wear_next_lecture);
        notificationBuilder.extend(new NotificationCompat.WearableExtender().setBackground(bm));
        return notificationBuilder.build();
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, CalendarActivity.class);
    }

    public void setLecture(String title, String date, String loc) {
        // Extract course title
        title = title.replaceAll("[A-Z 0-9(LV\\.Nr\\.)=]+$","");
        title = title.replaceAll("\\([A-Z]+[0-9]+\\)","");
        title = title.replaceAll("\\[[A-Z]+[0-9]+\\]","");

        mTitle = title.trim();
        mDate = Utils.getISODateTime(date);
        mLocation = loc;
    }
}
