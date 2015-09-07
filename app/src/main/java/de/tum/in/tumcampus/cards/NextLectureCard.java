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
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.CalendarActivity;
import de.tum.in.tumcampus.activities.RoomFinderDetailsActivity;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CardManager;


public class NextLectureCard extends Card {

    private static final String NEXT_LECTURE_DATE = "next_date";
    private final static int[] ids = {
            R.id.lecture_1,
            R.id.lecture_2,
            R.id.lecture_3,
            R.id.lecture_4
    };
    private TextView mLocation;
    private ArrayList<CalendarItem> lectures = new ArrayList<>();
    private TextView mTimeView;
    private int mSelected = 0;
    private TextView mEvent;

    public NextLectureCard(Context context) {
        super(context, "card_next_lecture");
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_next_lecture_item, parent, false);
        return new Card.CardViewHolder(view);
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_NEXT_LECTURE;
    }

    @Override
    public String getTitle() {
        return lectures.get(mSelected).title;
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        mCard = viewHolder.itemView;
        mLinearLayout = (LinearLayout) mCard.findViewById(R.id.card_view);
        mTitleView = (TextView) mCard.findViewById(R.id.card_title);
        mTimeView = (TextView) mCard.findViewById(R.id.card_time);
        mLocation = (TextView) mCard.findViewById(R.id.card_location_action);
        mEvent = (TextView) mCard.findViewById(R.id.card_event_action);

        showItem(0);

        int i = 0;
        if (lectures.size() > 1) {
            for (; i < lectures.size(); i++) {
                final int j = i;
                Button text = (Button) mCard.findViewById(ids[i]);
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showItem(j);
                    }
                });
            }
        }
        for (; i < 4; i++) {
            View text = mCard.findViewById(ids[i]);
            text.setVisibility(View.GONE);
        }
    }

    void showItem(int sel) {
        // Set selection on the buttons
        mSelected = sel;
        for (int i = 0; i < 4; i++) {
            mCard.findViewById(ids[i]).setSelected(i == sel);
        }

        final CalendarItem item = lectures.get(sel);

        // Set current title
        mTitleView.setText(getTitle());

        //Add content
        mTimeView.setText(DateUtils.getRelativeTimeSpanString(item.start.getTime(),
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));

        //Add location with link to room finder
        if (item.location != null) {
            mLocation.setText(item.location);
            mLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, RoomFinderDetailsActivity.class);
                    i.putExtra(RoomFinderDetailsActivity.EXTRA_LOCATION, item.location);
                    mContext.startActivity(i);
                }
            });
        } else {
            mLocation.setVisibility(View.GONE);
        }

        DateFormat week = new SimpleDateFormat("EEEE, ");
        DateFormat df = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        mEvent.setText(week.format(item.start) + df.format(item.start) + " - " + df.format(item.end));
        mEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, CalendarActivity.class);
                CalendarItem item = lectures.get(mSelected);
                i.putExtra(CalendarActivity.EVENT_TIME, item.start.getTime());
                mContext.startActivity(i);
            }
        });
    }

    @Override
    protected void discard(Editor editor) {
        CalendarItem item = lectures.get(lectures.size() - 1);
        editor.putLong(NEXT_LECTURE_DATE, item.start.getTime());
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        CalendarItem item = lectures.get(0);
        long prevTime = prefs.getLong(NEXT_LECTURE_DATE, 0);
        return (item.start.getTime() > prevTime);
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        CalendarItem item = lectures.get(0);
        final String time = DateUtils.getRelativeDateTimeString(mContext, item.start.getTime(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
        notificationBuilder.setContentText(item.title + "\n" + time);
        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.wear_next_lecture);
        notificationBuilder.extend(new NotificationCompat.WearableExtender().setBackground(bm));
        return notificationBuilder.build();
    }

    @Override
    public Intent getIntent() {
        return null;
    }

    public void setLectures(Cursor cur) {
        do {
            CalendarItem item = new CalendarItem();
            item.start = Utils.getISODateTime(cur.getString(1));
            item.end = Utils.getISODateTime(cur.getString(2));

            // Extract course title
            item.title = cur.getString(0);
            item.title = item.title.replaceAll("[A-Z, 0-9(LV\\.Nr\\.)=]+$", "");
            item.title = item.title.replaceAll("\\([A-Z]+[0-9]+\\)", "");
            item.title = item.title.replaceAll("\\[[A-Z]+[0-9]+\\]", "");
            item.title = item.title.trim();

            // Handle location
            item.location = cur.getString(3);
            if (item.location != null)
                item.location = item.location.replaceAll("\\([A-Z0-9\\.]+\\)", "").trim();
            else
                item.location = null;
            lectures.add(item);
        } while (cur.moveToNext());
        cur.close();
    }

    private class CalendarItem {
        String title;
        Date start;
        Date end;
        String location;
    }
}
