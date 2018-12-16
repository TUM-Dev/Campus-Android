package de.tum.in.tumcampusapp.component.tumui.roomfinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.tumui.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderSchedule;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
import de.tum.in.tumcampusapp.utils.Utils;

public class WeekViewFragment extends Fragment implements MonthLoader.MonthChangeListener {

    private final SparseArray<List<WeekViewDisplayable>> loadedEvents = new SparseArray<>();

    private String roomApiCode;
    private WeekView mWeekView;

    private Activity context;

    public static WeekViewFragment newInstance(String roomApiCode) {
        WeekViewFragment fragment = new WeekViewFragment();
        Bundle args = new Bundle();
        args.putString(Const.ROOM_ID, roomApiCode);
        fragment.setArguments(args);

        return fragment;
    }

    private static int calculateLoadedKey(Calendar start) {
        final int year = start.get(Calendar.YEAR);
        final int month = start.get(Calendar.MONTH);
        return (year * 16) | (month % 12);
    }

    @Override
    public List<WeekViewDisplayable> onMonthChange(Calendar startDate, Calendar endDate) {
        if (!isLoaded(startDate)) {
            loadEventsInBackground(startDate, endDate);
            return new ArrayList<>();
        }

        return loadedEvents.get(calculateLoadedKey(startDate));
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

            //Convert to the proper type
            final List<WeekViewDisplayable> events =
                    fetchEventList(roomApiCode, formattedStartTime, formattedEndTime);

            context.runOnUiThread(() -> {
                loadedEvents.put(calculateLoadedKey(start), events);
                mWeekView.notifyDataSetChanged();
            });
        }).start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.context = (Activity) context;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        roomApiCode = getArguments().getString(Const.ROOM_ID);

        View view = inflater.inflate(R.layout.fragment_day_view, container, false);
        mWeekView = view.findViewById(R.id.weekView);
        mWeekView.setMonthChangeListener(this);
        mWeekView.goToHour(8);
        return mWeekView;
    }

    private boolean isLoaded(Calendar start) {
        return loadedEvents.get(calculateLoadedKey(start)) != null;
    }

    private List<WeekViewDisplayable> fetchEventList(String roomId, String startDate, String endDate) {
        try {
            List<RoomFinderSchedule> schedules = TUMCabeClient
                    .getInstance(context)
                    .fetchSchedule(roomId, startDate, endDate);

            if (schedules == null) {
                return Collections.emptyList();
            }

            // Convert to the proper type
            List<WeekViewDisplayable> events = new ArrayList<>();
            for (RoomFinderSchedule schedule : schedules) {
                DateTime start = DateTimeUtils.INSTANCE.getDateTime(schedule.getStart());
                DateTime end = DateTimeUtils.INSTANCE.getDateTime(schedule.getEnd());

                IntegratedCalendarEvent calendarEvent =
                        new IntegratedCalendarEvent(String.valueOf(schedule.getEvent_id()), schedule.getTitle(), start,
                                end, "",
                                IntegratedCalendarEvent.getDisplayColorFromColor(
                                        ContextCompat.getColor(requireContext(), R.color.event_lecture)));
                events.add(calendarEvent);
            }

            return events;
        } catch (Exception e) {
            Utils.log(e);
        }
        return Collections.emptyList();
    }

}