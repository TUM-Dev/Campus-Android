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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest;

public class WeekViewFragment extends Fragment implements MonthLoader.MonthChangeListener {

    private final SparseArray<List<WeekViewEvent>> loadedEvents = new SparseArray<>();

    private String roomApiCode;
    private WeekView mWeekView;

    private Activity context;

    public static WeekViewFragment newInstance(String roomApiCode) {
        WeekViewFragment fragment = new WeekViewFragment();
        Bundle args = new Bundle();
        args.putString(TUMRoomFinderRequest.KEY_ROOM_ID, roomApiCode);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        roomApiCode = getArguments().getString(TUMRoomFinderRequest.KEY_ROOM_ID);

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

                List<IntegratedCalendarEvent> roomFinderResult = new ArrayList<>();
                TUMRoomFinderRequest request = new TUMRoomFinderRequest(getContext());
                request.fetchRoomSchedule(roomApiCode, Long.toString(startTime), Long.toString(endTime), roomFinderResult);

                //Convert to the proper type
                final List<WeekViewEvent> events = new ArrayList<>(roomFinderResult.size());
                for (IntegratedCalendarEvent event : roomFinderResult) {
                    events.add(event);
                }

                //Finish loading
                context.runOnUiThread(new Runnable() {
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
}
