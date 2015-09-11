package de.tum.in.tumcampus.auxiliary.calendar;

import android.content.Context;
import android.database.Cursor;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.Date;

import de.tum.in.tumcampus.models.managers.CalendarManager;

public class LoadEventsRequest implements EventLoader.LoadRequestClone {

    public int id;
    public int startDay;
    public int numDays;
    public ArrayList<Event> events;
    public Runnable successCallback;
    public Runnable cancelCallback;

    public LoadEventsRequest() {
    }

    public LoadEventsRequest(int id, int startDay, int numDays, ArrayList<Event> events,
                             final Runnable successCallback, final Runnable cancelCallback) {
        this.id = id;
        this.startDay = startDay;
        this.numDays = numDays;
        this.events = events;
        this.successCallback = successCallback;
        this.cancelCallback = cancelCallback;
    }

    @Override
    public EventLoader.LoadRequestClone clone(int id, int startDay, int numDays, ArrayList<Event> events,
                                              final Runnable successCallback, final Runnable cancelCallback) {
        return new LoadEventsRequest(id, startDay, numDays, events, successCallback, cancelCallback);
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

        CalendarManager cm = new CalendarManager(context);
        Time date = new Time();

        for(int curDay=startDay;curDay<startDay+days;curDay++) {
            date.setJulianDay(curDay);
            Cursor cEvents = cm.getFromDbForDate(new Date(date.toMillis(false)));

            while (cEvents.moveToNext())
                events.add(Event.generateEventFromCursor(cEvents));
        }
    }
}