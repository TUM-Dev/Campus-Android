package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.tumo.CalendarItem;

@Dao
public interface CalendarDao {
    @Query("SELECT c.* FROM calendar c WHERE status != 'CANCEL'")
    List<CalendarItem> getAllNotCancelled();

    @Query("SELECT c.* FROM calendar c WHERE dtstart LIKE '%' || :date || '%' AND status != 'CANCEL' ORDER BY dtstart ASC")
    List<CalendarItem> getAllByDateNotCancelled(String date);

    @Query("SELECT c.* FROM calendar c WHERE dtend BETWEEN :from AND :to "
           + "AND STATUS != 'CANCEL'"
           + "AND NOT EXISTS (SELECT * FROM widgets_timetable_blacklist WHERE widget_id = :widgetId"
           + "                AND lecture_title = c.title)"
           + "ORDER BY dtstart ASC")
    List<CalendarItem> getNextDays(String from, String to, String widgetId);

    @Query("SELECT c.* FROM calendar c WHERE datetime('now', 'localtime') BETWEEN dtstart AND dtend AND status != 'CANCEL'")
    List<CalendarItem> getCurrentLectures();

    @Query("SELECT COUNT(*) FROM calendar")
    boolean hasLectures();

    @Query("SELECT c.* FROM calendar c, widgets_timetable_blacklist " +
           "WHERE widget_id=:widgetId AND lecture_title=title " +
           "GROUP BY title")
    List<CalendarItem> getLecturesInBlacklist(String widgetId);

    @Query("SELECT c.* FROM calendar c " +
           "WHERE NOT EXISTS (SELECT * FROM widgets_timetable_blacklist " +
           "WHERE widget_id=:widgetId AND c.title=lecture_title) " +
           "GROUP BY c.title")
    List<CalendarItem> getLecturesNotInBlacklist(String widgetId);

    @Query("DELETE FROM calendar")
    void flush();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CalendarItem cal);

    @Query("SELECT c.* " +
           "FROM calendar c LEFT JOIN room_locations r ON " +
           "c.location=r.title " +
           "WHERE coalesce(r.latitude, '') = '' " +
           "GROUP BY c.location")
    List<CalendarItem> getLecturesWithoutCoordinates();

    @Query("SELECT c.* FROM calendar c JOIN " +
           "(SELECT dtstart AS maxstart FROM calendar WHERE status!='CANCEL' AND datetime('now', 'localtime')<dtstart " +
           "ORDER BY dtstart LIMIT 1) ON status!='CANCEL' AND datetime('now', 'localtime')<dtend AND dtstart<=maxstart " +
           "ORDER BY dtend, dtstart LIMIT 4")
    List<CalendarItem> getNextCalendarItems();

    @Query("SELECT * FROM calendar WHERE dtstart LIKE '%' || :start AND dtend LIKE '%' || :end ")
    CalendarItem getCalendarItemByStartAndEndTime(String start, String end);

    @Query("SELECT * FROM calendar WHERE nr=:id")
    CalendarItem getCalendarItemById(String id);
}
