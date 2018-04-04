package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.content.Context;
import android.graphics.Color;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.Calendar;

import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;

/**
 * A class to represent events for the integrated WeekView calendar
 */
public class IntegratedCalendarEvent extends WeekViewEvent {
    private static final float SATURATION_ADJUST = 1.3f;
    private static final float INTENSITY_ADJUST = 0.8f;
    private final String location;
    private boolean firstOnDay;

    public IntegratedCalendarEvent(CalendarItem calendarItem, Context context) {
        super(Long.parseLong(calendarItem.getNr()),
              calendarItem.getFormattedTitle(),
              calendarItem.getEventStart(),
              calendarItem.getEventEnd());

        this.location = calendarItem.getEventLocation();
        this.setColor(calendarItem.getEventColor(context));
    }

    public IntegratedCalendarEvent(long id, String title, Calendar startTime, Calendar endTime, String location, int color) {
        super(id, title, startTime, endTime);
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
