/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tum.in.tumcampus.auxiliary.calendar;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CalendarManager;

public class Event implements Cloneable {

    public long id;
    public int color;
    public CharSequence title;
    public CharSequence location;

    public int startDay;       // start Julian day
    public int endDay;         // end Julian day
    public int startTime;      // Start and end time are in minutes since midnight
    public int endTime;

    public long startMillis;   // UTC milliseconds since the epoch
    public long endMillis;     // UTC milliseconds since the epoch
    private int mColumn;
    private int mMaxColumns;

    // The coordinates of the event rectangle drawn on the screen.
    public float left;
    public float right;
    public float top;
    public float bottom;

    @Override
    public final Object clone() throws CloneNotSupportedException {
        super.clone();
        Event e = new Event();

        e.title = title;
        e.color = color;
        e.location = location;
        e.startDay = startDay;
        e.endDay = endDay;
        e.startTime = startTime;
        e.endTime = endTime;
        e.startMillis = startMillis;
        e.endMillis = endMillis;

        return e;
    }

    public final void copyTo(Event dest) {
        dest.id = id;
        dest.title = title;
        dest.color = color;
        dest.location = location;
        dest.startDay = startDay;
        dest.endDay = endDay;
        dest.startTime = startTime;
        dest.endTime = endTime;
        dest.startMillis = startMillis;
        dest.endMillis = endMillis;
    }

    public static Event newInstance() {
        Event e = new Event();
        e.id = 0;
        e.title = null;
        e.color = 0;
        e.location = null;
        e.startDay = 0;
        e.endDay = 0;
        e.startTime = 0;
        e.endTime = 0;
        e.startMillis = 0;
        e.endMillis = 0;

        return e;
    }

    /**
     * Loads <i>days</i> days worth of instances starting at <i>startDay</i>.
     */
    public static void loadEvents(Context context, ArrayList<Event> events, int startDay, int days) {
        events.clear();

        CalendarManager cm = new CalendarManager(context);
        Time date = new Time();

        for(int curDay=startDay;curDay<startDay+days;curDay++) {
            date.setJulianDay(curDay);
            Cursor cEvents = cm.getFromDbForDate(new Date(date.toMillis(false)));

            while (cEvents.moveToNext())
                events.add(generateEventFromCursor(cEvents));
        }
    }

    /**
     * @param cEvents Cursor pointing at event
     * @return An event created from the cursor
     */
    private static Event generateEventFromCursor(Cursor cEvents) {
        Event e = new Event();

        e.id = cEvents.getLong(0);
        String location = cEvents.getString(7);
        location = location.replaceAll("\\([A-Z0-9\\.]+\\)","");
        e.location = location.trim();

        String title = cEvents.getString(3);
        if(title.endsWith("VO") || title.endsWith("VU")) {
            e.color = getDisplayColorFromColor(0xff28921f);
        } else if(title.endsWith("UE")) {
            e.color = getDisplayColorFromColor(0xffFF8000);
        } else {
            e.color = getDisplayColorFromColor(0xff0000ff);
        }
        title = title.replaceAll("[A-Z 0-9(LV\\.Nr\\.)=]+$","");
        title = title.replaceAll("\\([A-Z]+[0-9]+\\)","");
        title = title.replaceAll("\\[[A-Z]+[0-9]+\\]","");
        e.title = title;

        String eStart = cEvents.getString(5);
        String eEnd = cEvents.getString(6);

        e.startMillis = Utils.getISODateTime(eStart).getTime();
        Time t = new Time();
        t.set(e.startMillis);
        e.startTime = t.hour*60+t.minute;
        Time current = new Time();
        current.set(System.currentTimeMillis());
        e.startDay = Time.getJulianDay(e.startMillis, current.gmtoff);

        e.endMillis = Utils.getISODateTime(eEnd).getTime();
        t = new Time();
        t.set(e.endMillis);
        e.endTime = t.hour*60+t.minute;
        e.endDay = Time.getJulianDay(e.endMillis, current.gmtoff);

        return e;
    }

    /**
     * Computes a position for each event.  Each event is displayed
     * as a non-overlapping rectangle.  For normal events, these rectangles
     * are displayed in separate columns in the week view and day view.  For
     * all-day events, these rectangles are displayed in separate rows along
     * the top.  In both cases, each event is assigned two numbers: N, and
     * Max, that specify that this event is the Nth event of Max number of
     * events that are displayed in a group. The width and position of each
     * rectangle depend on the maximum number of rectangles that occur at
     * the same time.
     *
     * @param eventsList the list of events, sorted into increasing time order
     * @param minimumDurationMillis minimum duration acceptable as cell height of each event
     * rectangle in millisecond. Should be 0 when it is not determined.
     */
    /* package */ static void computePositions(ArrayList<Event> eventsList,
            long minimumDurationMillis) {
        if (eventsList == null) {
            return;
        }

        // Compute the column positions separately for the all-day events
        doComputePositions(eventsList, minimumDurationMillis, false);
        doComputePositions(eventsList, minimumDurationMillis, true);
    }

    private static void doComputePositions(ArrayList<Event> eventsList,
            long minimumDurationMillis, boolean doAlldayEvents) {
        final ArrayList<Event> activeList = new ArrayList<Event>();
        final ArrayList<Event> groupList = new ArrayList<Event>();

        if (minimumDurationMillis < 0) {
            minimumDurationMillis = 0;
        }

        long colMask = 0;
        int maxCols = 0;
        for (Event event : eventsList) {
            // Process all-day events separately
            if (doAlldayEvents)
                continue;

            colMask = removeActiveEvents(
                    event, activeList.iterator(), minimumDurationMillis, colMask);

            // If the active list is empty, then reset the max columns, clear
            // the column bit mask, and empty the groupList.
            if (activeList.isEmpty()) {
                for (Event ev : groupList) {
                    ev.setMaxColumns(maxCols);
                }
                maxCols = 0;
                colMask = 0;
                groupList.clear();
            }

            // Find the first empty column.  Empty columns are represented by
            // zero bits in the column mask "colMask".
            int col = findFirstZeroBit(colMask);
            if (col == 64)
                col = 63;
            colMask |= (1L << col);
            event.setColumn(col);
            activeList.add(event);
            groupList.add(event);
            int len = activeList.size();
            if (maxCols < len)
                maxCols = len;
        }
        for (Event ev : groupList) {
            ev.setMaxColumns(maxCols);
        }
    }

    private static long removeActiveEvents(Event event, Iterator<Event> iter, long minDurationMillis, long colMask) {
        long start = event.getStartMillis();
        // Remove the inactive events. An event on the active list
        // becomes inactive when its end time is less than or equal to
        // the current event's start time.
        while (iter.hasNext()) {
            final Event active = iter.next();

            final long duration = Math.max(
                    active.getEndMillis() - active.getStartMillis(), minDurationMillis);
            if ((active.getStartMillis() + duration) <= start) {
                colMask &= ~(1L << active.getColumn());
                iter.remove();
            }
        }
        return colMask;
    }

    public static int findFirstZeroBit(long val) {
        for (int ii = 0; ii < 64; ++ii) {
            if ((val & (1L << ii)) == 0)
                return ii;
        }
        return 64;
    }

    public void setColumn(int column) {
        mColumn = column;
    }

    public int getColumn() {
        return mColumn;
    }

    public void setMaxColumns(int maxColumns) {
        mMaxColumns = maxColumns;
    }

    public int getMaxColumns() {
        return mMaxColumns;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public void setEndMillis(long endMillis) {
        this.endMillis = endMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    private static final float SATURATION_ADJUST = 1.3f;
    private static final float INTENSITY_ADJUST = 0.8f;
    private static int getDisplayColorFromColor(int color) {
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)) {
            return color;
        }

        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = Math.min(hsv[1] * SATURATION_ADJUST, 1.0f);
        hsv[2] = hsv[2] * INTENSITY_ADJUST;
        return Color.HSVToColor(hsv);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Event) {
            Event other = (Event) o;
            return other.id == this.id;
        }
        return false;
    }
}
