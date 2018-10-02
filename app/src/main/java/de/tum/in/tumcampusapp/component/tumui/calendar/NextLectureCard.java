package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.NextLectureCardViewHolder;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;

public class NextLectureCard extends Card {

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

    NextLectureCard(Context context) {
        super(CardManager.CARD_NEXT_LECTURE, context, "card_next_lecture");
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_next_lecture_item, parent, false);
        return new NextLectureCardViewHolder(view);
    }

    @Override
    public int getOptionsMenuResId() {
        return R.menu.card_popup_menu;
    }

    public CardCalendarItem getSelected() {
        return lectures.get(mSelected);
    }

    @Override
    public void updateViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof NextLectureCardViewHolder) {
            NextLectureCardViewHolder holder = (NextLectureCardViewHolder) viewHolder;
            holder.bind(lectures);
        }

        /*
        super.updateViewHolder(viewHolder);
        setCardView(viewHolder.itemView);
        setContentLayout(getCardView().findViewById(R.id.card_view));
        setTitleView(getCardView().findViewById(R.id.card_title));
        mTimeView = getCardView().findViewById(R.id.card_time);
        mLocation = getCardView().findViewById(R.id.card_location_action);
        mEvent = getCardView().findViewById(R.id.card_event_action);

        showItem(0);

        int i = 0;
        if (lectures.size() > 1) {
            for (; i < lectures.size(); i++) {
                final int j = i;
                Button text = getCardView().findViewById(IDS[i]);
                text.setOnClickListener(view -> showItem(j));
            }
        }
        for (; i < 4; i++) {
            View text = getCardView().findViewById(IDS[i]);
            text.setVisibility(View.GONE);
        }
        */
    }

    /*
    private void showItem(int selected) {
        // Set selection on the buttons
        mSelected = selected;
        for (int i = 0; i < 4; i++) {
            getCardView().findViewById(IDS[i])
                      .setSelected(i == selected);
        }

        final CalendarItem item = getSelected();

        // Set current title
        getTitleView().setText(item.title);

        //Add content
        mTimeView.setText(DateTimeUtils.INSTANCE.formatFutureTime(item.start, getContext()));

        //Add location with link to room finder
        if (item.location == null || item.location.isEmpty()) {
            mLocation.setVisibility(View.GONE);
        } else {
            mLocation.setText(item.location);
            mLocation.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString(SearchManager.QUERY, item.locationForSearch);
                NavigationDestination destination =
                        new SystemActivity(RoomFinderActivity.class, bundle);
                NavigationManager.INSTANCE.open(getContext(), destination);
            });
        }

        DateTimeFormatter dayOfWeek = DateTimeFormat.forPattern("EEEE, ").withLocale(Locale.getDefault());
        DateTimeFormatter time = DateTimeFormat.shortTime();
        mEvent.setText(String.format("%s%s - %s", dayOfWeek.print(item.start), time.print(item.start), time.print(item.end)));
        mEvent.setOnClickListener(view -> {
            Intent i = new Intent(getContext(), CalendarActivity.class);
            CalendarItem selectedItem = lectures.get(mSelected);
            i.putExtra(Const.EVENT_TIME, selectedItem.start.getMillis());
            getContext().startActivity(i);
        });
    }
    */

    @Override
    protected void discard(@NonNull SharedPreferences.Editor editor) {
        CardCalendarItem item = lectures.get(lectures.size() - 1);
        editor.putLong(NEXT_LECTURE_DATE, item.start.getMillis());
    }

    @Override
    protected boolean shouldShow(@NonNull SharedPreferences prefs) {
        CardCalendarItem item = lectures.get(0);
        long prevTime = prefs.getLong(NEXT_LECTURE_DATE, 0);
        return item.start.getMillis() > prevTime;
    }

    @Override
    public int getId() {
        return 0;
    }

    public void setLectures(List<CalendarItem> calendarItems) {
        for (CalendarItem calendarItem : calendarItems) {
            CardCalendarItem item = new CardCalendarItem();
            item.start = calendarItem.getDtstart();
            item.end = calendarItem.getDtend();

            // Extract course title
            item.title = calendarItem.getFormattedTitle();

            // Handle location
            item.location = calendarItem.getEventLocation();
            // This is the location in a format which is useful for searches:
            item.locationForSearch = calendarItem.getLocation();

            lectures.add(item);
        }
    }

    public static class CardCalendarItem {
        public String title;
        public DateTime start;
        public DateTime end;
        public String location;
        public String locationForSearch;
    }

}
