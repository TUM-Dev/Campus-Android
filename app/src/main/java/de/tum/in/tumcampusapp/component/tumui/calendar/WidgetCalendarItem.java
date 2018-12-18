package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.graphics.Color;

import com.alamkanak.weekview.WeekViewDisplayable;
import com.alamkanak.weekview.WeekViewEvent;

import org.joda.time.DateTime;

import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderSchedule;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;

/**
 * A class to represent events for the integrated WeekView calendar
 */
public class WidgetCalendarItem implements WeekViewDisplayable<WidgetCalendarItem> {

    private static final float SATURATION_ADJUST = 1.3f;
    private static final float INTENSITY_ADJUST = 0.8f;

    private final String id;
    private final String title;
    private final DateTime startTime;
    private final DateTime endTime;
    private final String location;

    private int color;
    private boolean firstOnDay;

    WidgetCalendarItem(CalendarItem calendarItem) {
        this(calendarItem.getNr(),
                calendarItem.getFormattedTitle(),
                calendarItem.getEventStart(),
                calendarItem.getEventEnd(),
                calendarItem.getEventLocation());
    }

    private WidgetCalendarItem(String id, String title, DateTime startTime,
                               DateTime endTime, String location) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    public static WidgetCalendarItem fromSchedule(RoomFinderSchedule schedule) {
        DateTime start = DateTimeUtils.INSTANCE.getDateTime(schedule.getStart());
        DateTime end = DateTimeUtils.INSTANCE.getDateTime(schedule.getEnd());

        // TODO int color = getDisplayColorFromColor(ContextCompat.getColor(context, R.color.event_lecture));

        return new WidgetCalendarItem(
                String.valueOf(schedule.getEvent_id()), schedule.getTitle(), start, end, "");
    }

    public static int getDisplayColorFromColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = Math.min(hsv[1] * SATURATION_ADJUST, 1.0f);
        hsv[2] *= INTENSITY_ADJUST;
        return Color.HSVToColor(hsv);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isFirstOnDay() {
        return firstOnDay;
    }

    public void setIsFirstOnDay(Boolean isFirstOnDay) {
        this.firstOnDay = isFirstOnDay;
    }

    @Override
    public WeekViewEvent<WidgetCalendarItem> toWeekViewEvent() {
        return new WeekViewEvent<>(Long.parseLong(id), title, startTime.toGregorianCalendar(),
                endTime.toGregorianCalendar(), location, color, false, this);
    }
}
