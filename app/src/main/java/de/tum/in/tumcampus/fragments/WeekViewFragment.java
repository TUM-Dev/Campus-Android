package de.tum.in.tumcampus.fragments;

import android.support.v4.app.Fragment;

import java.util.List;

import de.tum.in.tumcampus.auxiliary.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;

public class WeekViewFragment extends Fragment {

    private void loadEvents(List<IntegratedCalendarEvent> events, int startDay, int days) {
        events.clear();
/*
        Time date = new Time();
        date.setJulianDay(startDay);
        String start = Utils.getDateTimeString(new Date(date.toMillis(false)));
        date.setJulianDay(startDay+days-1);
        String end = Utils.getDateTimeString(new Date(date.toMillis(false)));
        TUMRoomFinderRequest request = new TUMRoomFinderRequest(getContext());
        request.fetchRoomSchedule(mRoomApi, start, end, events);
*/
    }
}
