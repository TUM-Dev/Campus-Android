package de.tum.in.tumcampusapp.auxiliary.calendar;

import android.graphics.Color;
import android.os.Build;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.Calendar;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumo.CalendarItem;

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

    public IntegratedCalendarEvent(CalendarItem calendarItem) {
        super(Long.parseLong(calendarItem.getNr()),
              formatEventTitle(calendarItem.getTitle()),
              calendarItem.getEventStart(),
              calendarItem.getEventEnd());

        this.location = getEventLocationFromCalendarItem(calendarItem);
        this.setColor(calendarItem.getEventColor());
    }

    public IntegratedCalendarEvent(long id, String title, Calendar startTime, Calendar endTime, String location, int color) {
        super(id, title, startTime, endTime);
        this.location = location;
        this.setColor(color);
    }

    private static String formatEventTitle(String eventTitle) {
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

    private static String getEventLocationFromCalendarItem(CalendarItem cEvents) {
        String eventLocation = cEvents.getLocation();
        if (eventLocation == null) {
            eventLocation = "";
        }
        eventLocation = COMPILE1.matcher(eventLocation)
                                .replaceAll("");
        return eventLocation.trim();
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
