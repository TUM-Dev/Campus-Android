package de.tum.in.tumcampus.cards;

import android.app.Notification;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.CalendarActivity;
import de.tum.in.tumcampus.activities.RoomFinderActivity;
import de.tum.in.tumcampus.models.managers.CardManager;


public class NextLectureCard extends Card {

    private static final String NEXT_LECTURE_DATE = "next_date";
    private static final String NEXT_LECTURE_TITLE = "next_title";
    private String mTitle;
    private Date mDate;
    private String mLocation;

    public NextLectureCard(Context context) {
        super(context, "card_next_lecture_setting");
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_NEXT_LECTURE;
    }

    @Override
    public String getTitle() {
        return mContext.getString(R.string.next_lecture);
    }

    @Override
    public View getCardView(Context context, ViewGroup parent) {
        super.getCardView(context, parent);

        //Add content
        final String time = DateUtils.getRelativeDateTimeString(mContext, mDate.getTime(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
        addTextView(mTitle + "\n" + time);

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
        return notificationBuilder.build();
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, CalendarActivity.class);
    }

    public void setLecture(String title, String date, String loc) {
        // Extract course title
        int end = title.indexOf('(');
        if (0 < end) {
            end = title.length();
        }
        mTitle = title.substring(0, end).trim();

        // Format Date
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            mDate = formatter.parse(date);
        } catch (ParseException e) {
            mDate = null;
        }

        // Check for Location
        mLocation = loc;
    }
}
