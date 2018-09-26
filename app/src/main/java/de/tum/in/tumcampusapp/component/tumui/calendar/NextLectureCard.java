package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.navigation.NavigationDestination;
import de.tum.in.tumcampusapp.component.other.navigation.NavigationManager;
import de.tum.in.tumcampusapp.component.other.navigation.SystemActivity;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;

public class NextLectureCard extends Card {

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

    NextLectureCard(Context context) {
        super(CardManager.CARD_NEXT_LECTURE, context, "card_next_lecture");
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_next_lecture_item, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public int getOptionsMenuResId() {
        return R.menu.card_popup_menu;
    }

    public CalendarItem getSelected() {
        return lectures.get(mSelected);
    }

    @Override
    public void updateViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
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

        final CalendarItem item = getSelected();

        // Set current title
        getMTitleView().setText(item.title);

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

    @Override
    protected void discard(@NonNull SharedPreferences.Editor editor) {
        CalendarItem item = lectures.get(lectures.size() - 1);
        editor.putLong(NEXT_LECTURE_DATE, item.start.getMillis());
    }

    @Override
    protected boolean shouldShow(@NonNull SharedPreferences prefs) {
        CalendarItem item = lectures.get(0);
        long prevTime = prefs.getLong(NEXT_LECTURE_DATE, 0);
        return item.start.getMillis() > prevTime;
    }

    @Override
    public int getId() {
        return 0;
    }

    public void setLectures(List<de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem> calendarItems) {
        for (de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem calendarItem : calendarItems) {
            CalendarItem item = new CalendarItem();
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

    private static class CalendarItem {
        String title;
        DateTime start;
        DateTime end;
        String location;
        String locationForSearch;
    }

}
