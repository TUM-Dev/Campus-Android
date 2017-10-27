package de.tum.in.tumcampusapp.auxiliary.calendar;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.Calendar;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * A class to represent events for the integrated WeekView calendar
 */
public class IntegratedCalendarEvent extends WeekViewEvent {
    private static final float SATURATION_ADJUST = 1.3f;
    private static final float INTENSITY_ADJUST = 0.8f;
    private static final Pattern COMPILE = Pattern.compile("[A-Z, 0-9(LV\\.Nr)=]+$");
    private static final Pattern PATTERN = Pattern.compile("\\([A-Z]+[0-9]+\\)");
    private static final Pattern COMPILE1 = Pattern.compile("\\([A-Z0-9\\.]+\\)");
    private final String location;
    private boolean isFirstOnDay;

    public IntegratedCalendarEvent(Cursor cEvents) {
        super(getEventIdFromCursor(cEvents),
              getEventTitleFromCursor(cEvents),
              getEventStartFromCursor(cEvents),
              getEventEndFromCursor(cEvents));

        this.location = getEventLocationFromCursor(cEvents);
        this.setColor(getEventColorFromCursor(cEvents));
    }

    public IntegratedCalendarEvent(long id, String title, Calendar startTime, Calendar endTime, String location, int color) {
        super(id, title, startTime, endTime);
        this.location = location;
        this.setColor(color);
    }

    private static String getEventTitleFromCursor(Cursor cEvents) {
        String eventTitle = cEvents.getString(3);
        if (eventTitle == null) {
            eventTitle = "";
        }
        eventTitle = COMPILE.matcher(eventTitle)
                            .replaceAll("");
        eventTitle = PATTERN.matcher(eventTitle)
                            .replaceAll("");
        eventTitle = PATTERN.matcher(eventTitle)
                            .replaceAll("");
        return eventTitle;
    }

    private static int getEventColorFromCursor(Cursor cEvents) {
        String eventTitle = cEvents.getString(3);
        if (eventTitle == null) {
            eventTitle = "";
        }
        if (eventTitle.endsWith("VO") || eventTitle.endsWith("VU")) {
            return getDisplayColorFromColor(0xff28921f);
        } else if (eventTitle.endsWith("UE")) {
            return getDisplayColorFromColor(0xffFF8000);
        } else {
            return getDisplayColorFromColor(0xff0000ff);
        }
    }

    private static String getEventLocationFromCursor(Cursor cEvents) {
        String eventLocation = cEvents.getString(7);
        if (eventLocation == null) {
            eventLocation = "";
        }
        eventLocation = COMPILE1.matcher(eventLocation)
                                .replaceAll("");
        return eventLocation.trim();
    }

    private static long getEventIdFromCursor(Cursor cEvents) {
        return cEvents.getLong(0);
    }

    private static Calendar getEventEndFromCursor(Cursor cEvents) {
        String eventEnd = cEvents.getString(6);
        Calendar result = Calendar.getInstance();
        result.setTime(Utils.getISODateTime(eventEnd));
        return result;
    }

    private static Calendar getEventStartFromCursor(Cursor cEvents) {
        String eventStart = cEvents.getString(5);
        Calendar result = Calendar.getInstance();
        result.setTime(Utils.getISODateTime(eventStart));
        return result;
    }

    public static int getDisplayColorFromColor(int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return color;
        } else {
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            hsv[1] = Math.min(hsv[1] * SATURATION_ADJUST, 1.0f);
            hsv[2] *= INTENSITY_ADJUST;
            return Color.HSVToColor(hsv);
        }
    }

    @Override
    public String getLocation() {
        return location;
    }

    public boolean isFirstOnDay() {
        return this.isFirstOnDay;
    }

    public void setIsFirstOnDay(Boolean isFirstOnDay) {
        this.isFirstOnDay = isFirstOnDay;
    }
}
