package de.tum.in.tumcampusapp.component.tumui.roomfinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewDisplayable;
import com.google.common.base.Optional;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    private static int calculateLoadedKey(int year, int month) {
        return (year * 16) | (month % 12);
    }

    @Override
    public List<WeekViewDisplayable> onMonthChange(int newYear, int newMonth) {
        if (!isLoaded(newYear, newMonth)) {
            loadEventsInBackground(newYear, newMonth);
            return new ArrayList<>();
        }

        //Events already have been loaded.
        return loadedEvents.get(calculateLoadedKey(newYear, newMonth));
    }

    private void loadEventsInBackground(final int newYear, final int newMonth) {
        new Thread(() -> {
            // Populate the week view with the events of the month to display
            DateTime start = new DateTime()
                    .withYear(newYear)
                    .withMonthOfYear(newMonth)
                    .withDayOfMonth(1);

            DateTimeFormatter format = DateTimeFormat.forPattern("yyyyMMdd")
                    .withLocale(Locale.getDefault());
            String startTime = format.print(start);

            int daysInMonth = start.dayOfMonth()
                    .getMaximumValue();
            DateTime end = start.withDayOfMonth(daysInMonth);
            String endTime = format.print(end);

            //Convert to the proper type
            final List<WeekViewDisplayable> events = fetchEventList(roomApiCode, startTime, endTime);

            //Finish loading
            context.runOnUiThread(() -> {
                loadedEvents.put(calculateLoadedKey(newYear, newMonth), events);
                //Trigger onMonthChange() again
                mWeekView.notifyDatasetChanged();
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

    private boolean isLoaded(int year, int month) {
        return loadedEvents.get(calculateLoadedKey(year, month)) != null;
    }

    private List<WeekViewDisplayable> fetchEventList(String roomId, String startDate, String endDate) {
        List<WeekViewDisplayable> events = new ArrayList<>();
        try {
            Optional<List<RoomFinderSchedule>> result = Optional.of(TUMCabeClient.getInstance(context)
                    .fetchSchedule(roomId, startDate, endDate));
            List<RoomFinderSchedule> schedules = result.get();

            //Convert to the proper type
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

        } catch (IOException | NullPointerException | IllegalStateException e) {
            Utils.log(e);
        }
        return events;
    }

}