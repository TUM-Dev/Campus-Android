package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.overview.card.NotificationAwareCard;
import de.tum.in.tumcampusapp.utils.DateUtils;

public class NextLectureCard extends NotificationAwareCard {

    private static final String NEXT_LECTURE_DATE = "next_date";
    private final static int[] IDS = {
            R.id.lecture_1,
            R.id.lecture_2,
            R.id.lecture_3,
            R.id.lecture_4
    };
    private TextView mLocation;
    private final List<CardCalendarItem> lectures = new ArrayList<>();
    private TextView mTimeView;
    private int mSelected;
    private TextView mEvent;

    public NextLectureCard(Context context) {
        super(CardManager.CARD_NEXT_LECTURE, context, "card_next_lecture");
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_next_lecture_item, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public String getTitle() {
        return lectures.get(mSelected).title;
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        setMCard(viewHolder.itemView);
        setMLinearLayout(getMCard().findViewById(R.id.card_view));
        setMTitleView(getMCard().findViewById(R.id.card_title));
        mTimeView = getMCard().findViewById(R.id.card_time);
        mLocation = getMCard().findViewById(R.id.card_location_action);
        mEvent = getMCard().findViewById(R.id.card_event_action);

        showItem(0);

        int i = 0;
        if (lectures.size() > 1) {
            for (; i < lectures.size(); i++) {
                final int j = i;
                Button text = getMCard().findViewById(IDS[i]);
                text.setOnClickListener(view -> showItem(j));
            }
        }
        for (; i < 4; i++) {
            View text = getMCard().findViewById(IDS[i]);
            text.setVisibility(View.GONE);
        }
    }

    private void showItem(int sel) {
        // Set selection on the buttons
        mSelected = sel;
        for (int i = 0; i < 4; i++) {
            getMCard().findViewById(IDS[i])
                      .setSelected(i == sel);
        }

        final CardCalendarItem item = lectures.get(sel);

        // Set current title
        getMTitleView().setText(getTitle());

        //Add content
        mTimeView.setText(DateUtils.getFutureTime(item.start, getContext()));

        //Add location with link to room finder
        if (item.location == null || item.location.isEmpty()) {
            mLocation.setVisibility(View.GONE);
        } else {
            mLocation.setText(item.location);
            mLocation.setOnClickListener(v -> {
                Intent i = new Intent(getContext(), RoomFinderActivity.class);
                i.putExtra(SearchManager.QUERY, item.location);
                getContext().startActivity(i);
            });
        }

        DateFormat week = new SimpleDateFormat("EEEE, ", Locale.getDefault());
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
        mEvent.setText(String.format("%s%s - %s", week.format(item.start), df.format(item.start), df.format(item.end)));
        mEvent.setOnClickListener(view -> {
            Intent i = new Intent(getContext(), CalendarActivity.class);
            CardCalendarItem item1 = lectures.get(mSelected);
            i.putExtra(CalendarActivity.EVENT_TIME, item1.start.getTime());
            getContext().startActivity(i);
        });
    }

    @Override
    protected void discard(Editor editor) {
        CardCalendarItem item = lectures.get(lectures.size() - 1);
        editor.putLong(NEXT_LECTURE_DATE, item.start.getTime());
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        CardCalendarItem item = lectures.get(0);
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

    /*
    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        CardCalendarItem item = lectures.get(0);
        final String time = DateUtils.getFutureTime(item.start, getContext());
        notificationBuilder.setContentText(item.title + '\n' + time);
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        Bitmap bm = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.wear_next_lecture);
        notificationBuilder.extend(new NotificationCompat.WearableExtender().setBackground(bm));
        return notificationBuilder.build();
    }
    */

    public void setLectures(List<CalendarItem> calendarItems) {
        for (CalendarItem calendarItem : calendarItems) {
            lectures.add(CardCalendarItem.fromCalendarItem(calendarItem));
        }
    }

    public static class CardCalendarItem {
        public String title;
        public Date start;
        public Date end;
        public String location;

        public static CardCalendarItem fromCalendarItem(CalendarItem calendarItem) {
            CardCalendarItem item = new CardCalendarItem();
            item.start = DateUtils.getDateTime(calendarItem.getDtstart());
            item.end = DateUtils.getDateTime(calendarItem.getDtend());

            // Extract course title
            item.title = calendarItem.getFormattedTitle();

            // Handle location
            item.location = calendarItem.getEventLocation();
            return item;
        }
    }

    @Override
    public RemoteViews getRemoteViews(Context context, int appWidgetId) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.cards_widget_card);
        remoteViews.setTextViewText(R.id.widgetCardTextView, this.getTitle());
        remoteViews.setImageViewResource(R.id.widgetCardImageView, R.drawable.ic_my_lectures);
        return remoteViews;
    }
}
