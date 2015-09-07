package de.tum.in.tumcampus.auxiliary.calendar;

import android.content.Context;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.Date;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;

@SuppressWarnings("ALL")
public class LoadEventsRequestTimeTable implements EventLoader.LoadRequestClone {

    public int id;
    public int startDay;
    public int numDays;
    public ArrayList<Event> events;
    public Runnable successCallback;
    public Runnable cancelCallback;
    String mRoomApi;

    public LoadEventsRequestTimeTable(String api) {
        mRoomApi = api;
    }

    public LoadEventsRequestTimeTable(int id, int startDay, int numDays, ArrayList<Event> events,
                                      final Runnable successCallback, final Runnable cancelCallback, String roomApi) {
        this.id = id;
        this.startDay = startDay;
        this.numDays = numDays;
        this.events = events;
        this.successCallback = successCallback;
        this.cancelCallback = cancelCallback;
        this.mRoomApi = roomApi;
    }

    @Override
    public EventLoader.LoadRequestClone clone(int id, int startDay, int numDays, ArrayList<Event> events,
                                              final Runnable successCallback, final Runnable cancelCallback) {
        return new LoadEventsRequestTimeTable(id, startDay, numDays, events, successCallback, cancelCallback, mRoomApi);
    }

    public void processRequest(EventLoader eventLoader) {
        loadEvents(eventLoader.mContext, events, startDay, numDays);

        // Check if we are still the most recent request.
        if (id == eventLoader.mSequenceNumber.get()) {
            eventLoader.mHandler.post(successCallback);
        } else {
            eventLoader.mHandler.post(cancelCallback);
        }
    }

    public void skipRequest(EventLoader eventLoader) {
        eventLoader.mHandler.post(cancelCallback);
    }


    /**
     * Loads <i>days</i> days worth of instances starting at <i>startDay</i>.
     */
    public void loadEvents(Context context, ArrayList<Event> events, int startDay, int days) {
        events.clear();

        Time date = new Time();
        date.setJulianDay(startDay);
        String start = Utils.getDateTimeString(new Date(date.toMillis(false)));
        date.setJulianDay(startDay+days-1);
        String end = Utils.getDateTimeString(new Date(date.toMillis(false)));
        TUMRoomFinderRequest request = new TUMRoomFinderRequest(context);
        request.fetchRoomSchedule(mRoomApi, start, end, events);
    }
}