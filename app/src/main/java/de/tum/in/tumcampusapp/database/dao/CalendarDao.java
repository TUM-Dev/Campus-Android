package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.List;

import de.tum.in.tumcampusapp.models.dbEntities.RoomLocations;
import de.tum.in.tumcampusapp.models.tumo.CalendarItem;

@Dao
public interface CalendarDao {
    @Query("SELECT * FROM calendar WHERE status != 'CANCEL'")
    List<CalendarItem> getAllNotCancelled();

    @Query("SELECT * FROM calendar WHERE dtstart LIKE :date AND status != 'CANCEL' ORDER BY dtstart ASC")
    List<CalendarItem> getAllByDateNotCancelled(String date);

    @Query("SELECT * FROM calendar c WHERE dtend BETWEEN :from AND :to "
           + "AND STATUS != 'CANCEL'"
           + "AND NOT EXISTS (SELECT * FROM widgets_timetable_blacklist WHERE widget_id = :widgetId"
           + "                AND lecture_title = c.title)"
           + "ORDER BY dtstart ASC")
    List<CalendarItem> getNextDays(String from, String to, String widgetId);

    @Query("SELECT * FROM calendar WHERE datetime('now', 'localtime') BETWEEN dtstart AND dtend AND status != 'CANCEL'")
    List<CalendarItem> getCurrentLectures();

    @Query("SELECT COUNT(*) FROM calendar LIMIT 1")
    int lectureCount();

    @Query("SELECT * FROM calendar, widgets_timetable_blacklist " +
           "WHERE widget_id=:widgetId AND lecture_title=title " +
           "GROUP BY title")
    List<CalendarItem> getLecturesWithBlacklist(String widgetId);

    @Query("SELECT * FROM calendar GROUP BY title")
    List<CalendarItem> getDistinctLectures();

    @Query("DELETE FROM calendar")
    void flush();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CalendarItem cal);

    @Query("SELECT * " +
           "FROM calendar c LEFT JOIN room_locations r ON " +
           "c.location=r.title " +
           "WHERE r.latitude = '' " +
           "GROUP BY c.location")
    List<CalendarItem> getLecturesWithoutCoordinates();

    @Query("SELECT * FROM calendar JOIN " +
           "(SELECT dtstart AS maxstart FROM calendar WHERE status!='CANCEL' AND datetime('now', 'localtime')<dtstart " +
           "ORDER BY dtstart LIMIT 1) ON status!='CANCEL' AND datetime('now', 'localtime')<dtend AND dtstart<=maxstart " +
           "ORDER BY dtend, dtstart LIMIT 4")
    List<CalendarItem> getNextCalendarItem();
}
