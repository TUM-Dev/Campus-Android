package de.tum.in.tumcampusapp.component.tumui.calendar;

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
        String id = Long.toString(schedule.getEvent_id());
        DateTime start = DateTimeUtils.INSTANCE.getDateTime(schedule.getStart());
        DateTime end = DateTimeUtils.INSTANCE.getDateTime(schedule.getEnd());
        return new WidgetCalendarItem(id, schedule.getTitle(), start, end, "");
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public int getColor() {
        return color;
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
