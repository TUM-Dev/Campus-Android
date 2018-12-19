package de.tum.in.tumcampusapp.component.tumui.roomfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewDisplayable;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.tumui.calendar.WidgetCalendarItem;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderSchedule;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

public class WeekViewFragment extends Fragment
        implements MonthLoader.MonthChangeListener<WidgetCalendarItem> {

    private final LongSparseArray<List<WeekViewDisplayable<WidgetCalendarItem>>> loadedEvents = new LongSparseArray<>();

    private String roomApiCode;
    private WeekView<WidgetCalendarItem> mWeekView;

    public static WeekViewFragment newInstance(String roomApiCode) {
        WeekViewFragment fragment = new WeekViewFragment();
        Bundle args = new Bundle();
        args.putString(Const.ROOM_ID, roomApiCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomApiCode = getArguments().getString(Const.ROOM_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mWeekView = view.findViewById(R.id.weekView);
        mWeekView.setMonthChangeListener(this);
        mWeekView.goToHour(8);
    }

    @Override
    public List<WeekViewDisplayable<WidgetCalendarItem>> onMonthChange(Calendar startDate, Calendar endDate) {
        if (!isLoaded(startDate)) {
            loadEventsInBackground(startDate, endDate);
            return new ArrayList<>();
        }
        return loadedEvents.get(startDate.getTimeInMillis());
    }

    private boolean isLoaded(Calendar start) {
        return loadedEvents.get(start.getTimeInMillis()) != null;
    }

    private void loadEventsInBackground(final Calendar start, final Calendar end) {
        // Populate the week view with the events of the month to display
        new Thread(() -> {
            DateTimeFormatter format = DateTimeFormat.forPattern("yyyyMMdd")
                    .withLocale(Locale.getDefault());

            DateTime startTime = new DateTime(start);
            DateTime endTime = new DateTime(end);

            String formattedStartTime = format.print(startTime);
            String formattedEndTime = format.print(endTime);

            // Convert to the proper type
            final List<WeekViewDisplayable<WidgetCalendarItem>> events =
                    fetchEventList(roomApiCode, formattedStartTime, formattedEndTime);

            requireActivity().runOnUiThread(() -> {
                loadedEvents.put(start.getTimeInMillis(), events);
                mWeekView.notifyDataSetChanged();
            });
        }).start();
    }

    private List<WeekViewDisplayable<WidgetCalendarItem>> fetchEventList(
            String roomId, String startDate, String endDate) {
        try {
            List<RoomFinderSchedule> schedules = TUMCabeClient
                    .getInstance(requireActivity())
                    .fetchSchedule(roomId, startDate, endDate);

            if (schedules == null) {
                return Collections.emptyList();
            }

            // Convert to the proper type
            List<WeekViewDisplayable<WidgetCalendarItem>> events = new ArrayList<>();
            for (RoomFinderSchedule schedule : schedules) {
                WidgetCalendarItem calendarItem = WidgetCalendarItem.create(schedule);
                calendarItem.setColor(ContextCompat.getColor(requireContext(), R.color.event_lecture));
                events.add(calendarItem);
            }

            return events;
        } catch (Exception e) {
            Utils.log(e);
        }

        return Collections.emptyList();
    }

}