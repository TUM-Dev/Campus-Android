package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.content.Context;
import android.graphics.Color;

import com.alamkanak.weekview.WeekViewEvent;

import org.joda.time.DateTime;

import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;

/**
 * A class to represent events for the integrated WeekView calendar
 */
public class IntegratedCalendarEvent extends WeekViewEvent {
    private static final float SATURATION_ADJUST = 1.3f;
    private static final float INTENSITY_ADJUST = 0.8f;
    private final String location;
    private boolean firstOnDay;

    IntegratedCalendarEvent(CalendarItem calendarItem, Context context) {
        this(calendarItem.getNr(),
                calendarItem.getFormattedTitle(),
                calendarItem.getEventStart(),
                calendarItem.getEventEnd(),
                calendarItem.getEventLocation(),
                calendarItem.getEventColor(context));
    }

    public IntegratedCalendarEvent(String id, String title, DateTime startTime, DateTime endTime, String location, int color) {
        super(Long.parseLong(id), title, startTime.toGregorianCalendar(), endTime.toGregorianCalendar());
        this.location = location;
        this.setColor(color);
    }

    public static int getDisplayColorFromColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = Math.min(hsv[1] * SATURATION_ADJUST, 1.0f);
        hsv[2] *= INTENSITY_ADJUST;
        return Color.HSVToColor(hsv);
    }

    @Override
    public String getLocation() {
        return location;
    }

    public boolean isFirstOnDay() {
        return firstOnDay;
    }

    public void setIsFirstOnDay(Boolean isFirstOnDay) {
        this.firstOnDay = isFirstOnDay;
    }
}
