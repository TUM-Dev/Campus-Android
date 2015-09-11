package de.tum.in.tumcampus.auxiliary.calendar;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.Calendar;

import de.tum.in.tumcampus.auxiliary.Utils;


/**
 * A class to represent events for the integrated WeekView calendar
 */
public class IntegratedCalendarEvent extends WeekViewEvent {

    String location;

    public IntegratedCalendarEvent(Cursor cEvents) {
        super(getEventIdFromCursor(cEvents),
                getEventTitleFromCursor(cEvents),
                getEventStartFromCursor(cEvents),
                getEventEndFromCursor(cEvents));

        this.location = getEventLocationFromCursor(cEvents);
        this.setColor(getEventColorFromCursor(cEvents));

    }

    private static String getEventTitleFromCursor(Cursor cEvents) {
        String eventTitle = cEvents.getString(3);
        if (eventTitle == null) eventTitle = "";
        eventTitle = eventTitle.replaceAll("[A-Z, 0-9(LV\\.Nr)=]+$", "");
        eventTitle = eventTitle.replaceAll("\\([A-Z]+[0-9]+\\)", "");
        eventTitle = eventTitle.replaceAll("\\[[A-Z]+[0-9]+\\]", "");
        return eventTitle;
    }

    private static int getEventColorFromCursor(Cursor cEvents) {
        String eventTitle = cEvents.getString(3);
        if (eventTitle == null) eventTitle = "";
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
        if (eventLocation == null) eventLocation = "";
        eventLocation = eventLocation.replaceAll("\\([A-Z0-9\\.]+\\)", "");
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
}
