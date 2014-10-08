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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.CalendarActivity;
import de.tum.in.tumcampus.activities.RoomFinderDetailsActivity;
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
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCard = mInflater.inflate(R.layout.card_next_lecture_item, parent, false);
        mLinearLayout = (LinearLayout) mCard.findViewById(R.id.card_view);
        mTitleView = (TextView) mCard.findViewById(R.id.card_title);
        mTitleView.setText(getTitle());
        TextView timeView = (TextView) mCard.findViewById(R.id.card_time);

        //Add content
        final String time = DateUtils.getRelativeDateTimeString(mContext, mDate.getTime(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
        timeView.setText(time);

        //Add location with link to room finder
        if (mLocation != null) {
            TextView location = (TextView)mCard.findViewById(R.id.card_location_action);
            location.setText(mLocation);
            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, RoomFinderDetailsActivity.class);
                    i.putExtra(RoomFinderDetailsActivity.EXTRA_LOCATION, mLocation);
                    mContext.startActivity(i);
                }
            });
            location.setVisibility(View.VISIBLE);
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
        Intent i = new Intent(mContext, CalendarActivity.class);
        i.putExtra(CalendarActivity.EVENT_TIME, mDate.getTime());
        return i;
    }

    public void setLecture(String title, String date, String loc) {
        // Extract course title
        title = title.replaceAll("[A-Z 0-9(LV\\.Nr\\.)=]+$","");
        title = title.replaceAll("\\([A-Z]+[0-9]+\\)","");
        title = title.replaceAll("\\[[A-Z]+[0-9]+\\]","");

        mTitle = title.trim();
        mDate = Utils.getISODateTime(date);
        if(loc!=null)
            mLocation = loc.replaceAll("\\([A-Z0-9\\.]+\\)","").trim();
        else
            mLocation = null;
    }
}
