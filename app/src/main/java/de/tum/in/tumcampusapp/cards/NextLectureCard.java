package de.tum.in.tumcampusapp.cards;

import android.app.Notification;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.CalendarActivity;
import de.tum.in.tumcampusapp.activities.RoomFinderActivity;
import de.tum.in.tumcampusapp.auxiliary.DateUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.cards.generic.NotificationAwareCard;
import de.tum.in.tumcampusapp.managers.CardManager;

public class NextLectureCard extends NotificationAwareCard {

    private static final String NEXT_LECTURE_DATE = "next_date";
    private final static int[] IDS = {
            R.id.lecture_1,
            R.id.lecture_2,
            R.id.lecture_3,
            R.id.lecture_4
    };
    private TextView mLocation;
    private final List<CalendarItem> lectures = new ArrayList<>();
    private TextView mTimeView;
    private int mSelected;
    private TextView mEvent;

    public NextLectureCard(Context context) {
        super(CardManager.CARD_NEXT_LECTURE, context, "card_next_lecture");
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_next_lecture_item, parent, false);
        return new Card.CardViewHolder(view);
    }

    @Override
    public String getTitle() {
        return lectures.get(mSelected).title;
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        mCard = viewHolder.itemView;
        mLinearLayout = mCard.findViewById(R.id.card_view);
        mTitleView = mCard.findViewById(R.id.card_title);
        mTimeView = mCard.findViewById(R.id.card_time);
        mLocation = mCard.findViewById(R.id.card_location_action);
        mEvent = mCard.findViewById(R.id.card_event_action);

        showItem(0);

        int i = 0;
        if (lectures.size() > 1) {
            for (; i < lectures.size(); i++) {
                final int j = i;
                Button text = mCard.findViewById(IDS[i]);
                text.setOnClickListener(view -> showItem(j));
            }
        }
        for (; i < 4; i++) {
            View text = mCard.findViewById(IDS[i]);
            text.setVisibility(View.GONE);
        }
    }

    private void showItem(int sel) {
        // Set selection on the buttons
        mSelected = sel;
        for (int i = 0; i < 4; i++) {
            mCard.findViewById(IDS[i])
                 .setSelected(i == sel);
        }

        final CalendarItem item = lectures.get(sel);

        // Set current title
        mTitleView.setText(getTitle());

        //Add content
        mTimeView.setText(DateUtils.getFutureTime(item.start, mContext));

        //Add location with link to room finder
        if (item.location == null || item.location.isEmpty()) {
            mLocation.setVisibility(View.GONE);
        } else {
            mLocation.setText(item.location);
            mLocation.setOnClickListener(v -> {
                Intent i = new Intent(mContext, RoomFinderActivity.class);
                i.putExtra(SearchManager.QUERY, item.location);
                mContext.startActivity(i);
            });
        }

        DateFormat week = new SimpleDateFormat("EEEE, ", Locale.getDefault());
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
        mEvent.setText(String.format("%s%s - %s", week.format(item.start), df.format(item.start), df.format(item.end)));
        mEvent.setOnClickListener(view -> {
            Intent i = new Intent(mContext, CalendarActivity.class);
            CalendarItem item1 = lectures.get(mSelected);
            i.putExtra(CalendarActivity.EVENT_TIME, item1.start.getTime());
            mContext.startActivity(i);
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
        return item.start.getTime() > prevTime;
    }

    @Override
    public Intent getIntent() {
        return null;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        CalendarItem item = lectures.get(0);
        final String time = DateUtils.getFutureTime(item.start, mContext);
        notificationBuilder.setContentText(item.title + '\n' + time);
        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.wear_next_lecture);
        notificationBuilder.extend(new NotificationCompat.WearableExtender().setBackground(bm));
        return notificationBuilder.build();
    }

    public void setLectures(Cursor cur) {
        do {
            CalendarItem item = new CalendarItem();
            item.start = Utils.getISODateTime(cur.getString(1));
            item.end = Utils.getISODateTime(cur.getString(2));

            // Extract course title
            item.title = cur.getString(0);
            item.title = item.title.replaceAll("[A-Z, 0-9(LV\\.Nr)=]+$", "");
            item.title = item.title.replaceAll("\\([A-Z]+[0-9]+\\)", "");
            item.title = item.title.replaceAll("\\[[A-Z]+[0-9]+\\]", "");
            item.title = item.title.trim();

            // Handle location
            item.location = cur.getString(3);
            if (item.location != null) {
                item.location = item.location.replaceAll("\\([A-Z0-9\\.]+\\)", "")
                                             .trim();
            }
            lectures.add(item);
        } while (cur.moveToNext());
        cur.close();
    }

    private static class CalendarItem {
        String title;
        Date start;
        Date end;
        String location;
    }

    @Override
    public RemoteViews getRemoteViews(Context context) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.cards_widget_card);
        remoteViews.setTextViewText(R.id.widgetCardTextView, this.getTitle());
        remoteViews.setImageViewResource(R.id.widgetCardImageView, R.drawable.ic_my_lectures);
        return remoteViews;
    }
}
