package de.tum.in.tumcampusapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.common.base.Optional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.auxiliary.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderSchedule;

public class WeekViewFragment extends Fragment implements MonthLoader.MonthChangeListener {

    private final SparseArray<List<WeekViewEvent>> loadedEvents = new SparseArray<>();

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        roomApiCode = getArguments().getString(Const.ROOM_ID);

        View view = inflater.inflate(R.layout.fragment_day_view, container, false);
        mWeekView = view.findViewById(R.id.weekView);
        mWeekView.setMonthChangeListener(this);
        mWeekView.goToHour(8);
        return mWeekView;
    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
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
            Calendar calendar = Calendar.getInstance();
            //Note the (-1), since the calendar starts with month 0, but we get months starting with 1
            calendar.set(newYear, newMonth - 1, 1);
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String startTime = format.format(calendar.getTime());

            calendar.set(newYear, newMonth - 1, daysInMonth);
            String endTime = format.format(calendar.getTime());

            //Convert to the proper type
            final List<WeekViewEvent> events = fetchEventList(roomApiCode, startTime, endTime);

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

    private boolean isLoaded(int year, int month) {
        return loadedEvents.get(calculateLoadedKey(year, month)) != null;
    }

    private static int calculateLoadedKey(int year, int month) {
        return (year * 16) | (month % 12);
    }

    private List<WeekViewEvent> fetchEventList(String roomId, String startDate, String endDate) {
        List<WeekViewEvent> events = new ArrayList<>();
        try {
            Optional<List<RoomFinderSchedule>> result = Optional.of(TUMCabeClient.getInstance(context)
                                                                                 .fetchSchedule(roomId, startDate, endDate));
            List<RoomFinderSchedule> schedules = result.get();

            //Convert to the proper type
            for (RoomFinderSchedule schedule : schedules) {
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(Utils.getDateTime(schedule.getStart()));

                Calendar endCal = Calendar.getInstance();
                endCal.setTime(Utils.getDateTime(schedule.getEnd()));

                IntegratedCalendarEvent calendarEvent = new IntegratedCalendarEvent(schedule.getEvent_id(),
                                                                                    schedule.getTitle(), startCal, endCal, "",
                                                                                    IntegratedCalendarEvent.getDisplayColorFromColor(0xff28921f));

                events.add(calendarEvent);
            }

            return events;

        } catch (IOException | NullPointerException e) {
            Utils.log(e);
        }
        return events;
    }

}