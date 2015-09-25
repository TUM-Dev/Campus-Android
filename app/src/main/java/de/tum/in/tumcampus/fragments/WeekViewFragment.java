package de.tum.in.tumcampus.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;

public class WeekViewFragment extends Fragment implements WeekView.MonthChangeListener {

    private HashMap<Integer, List<WeekViewEvent>> loadedEvents = new HashMap<>();

    private String roomApiCode;
    private WeekView mWeekView;

    public static WeekViewFragment newInstance(String roomApiCode) {
        WeekViewFragment fragment = new WeekViewFragment();
        Bundle args = new Bundle();
        args.putString(TUMRoomFinderRequest.KEY_ROOM_API_CODE, roomApiCode);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        roomApiCode = getArguments().getString(TUMRoomFinderRequest.KEY_ROOM_API_CODE);

        View view = inflater.inflate(R.layout.fragment_day_view, container, false);
        mWeekView = (WeekView) view.findViewById(R.id.weekView);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Populate the week view with the events of the month to display
                Calendar calendar = Calendar.getInstance();
                //Note the (-1), since the calendar starts with month 0, but we get months starting with 1
                calendar.set(newYear, newMonth - 1, 1);
                int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                long startTime = calendar.getTimeInMillis();
                calendar.set(newYear, newMonth - 1, daysInMonth);
                long endTime = calendar.getTimeInMillis();

                ArrayList<IntegratedCalendarEvent> roomFinderResult = new ArrayList<>();
                TUMRoomFinderRequest request = new TUMRoomFinderRequest(getContext());
                request.fetchRoomSchedule(roomApiCode, Long.toString(startTime), Long.toString(endTime), roomFinderResult);

                //Convert to the proper type
                final List<WeekViewEvent> events = new ArrayList<>(roomFinderResult.size());
                for (IntegratedCalendarEvent event : roomFinderResult) {
                    events.add(event);
                }

                //Finish loading
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadedEvents.put(calculateLoadedKey(newYear, newMonth), events);
                        //Trigger onMonthChange() again
                        mWeekView.notifyDatasetChanged();
                    }
                });
            }
        }).start();
    }

    private boolean isLoaded(int year, int month) {
        return loadedEvents.containsKey(calculateLoadedKey(year, month));
    }

    private static int calculateLoadedKey(int year, int month) {
        return (year * 16) | (month % 12);
    }
}
