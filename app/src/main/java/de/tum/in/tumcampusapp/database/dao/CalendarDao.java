package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import de.tum.in.tumcampusapp.models.tumo.Calendar;

@Dao
public interface CalendarDao {
    @Query("SELECT * FROM calendar WHERE status != 'CANCEL'")
    Cursor getAllNotCancelled(); // TODO: replace cursor with object list

    @Query("SELECT * FROM calendar WHERE dtstart LIKE :date AND status != 'CANCEL' ORDER BY dtstart ASC")
    Cursor getAllByDateNotCancelled(String date);

    /*
    "SELECT * FROM calendar c WHERE dtend BETWEEN ? AND ? AND status!='CANCEL' " +
                                         "AND NOT EXISTS (SELECT * FROM widgets_timetable_blacklist WHERE widget_id=? AND lecture_title=c.title) " +
                                         "ORDER BY dtstart ASC", new String[]{from, to, String.valueOf(widgetId)})
     */
    @Query("SELECT * FROM calendar c WHERE dtend BETWEEN :from AND :to "
           + "AND STATUS != 'CANCEL'"
           + "AND NOT EXISTS (SELECT * FROM widgets_timetable_blacklist WHERE widget_id = :widgetId"
           + "                AND lecture_title = c.title)"
           + "ORDER BY dtstart ASC")
    Cursor getNextDays(String from, String to, String widgetId);

    @Query("SELECT title, location, nr, dtend FROM calendar WHERE datetime('now', 'localtime') BETWEEN dtstart AND dtend AND status != 'CANCEL'")
    Cursor getCurrentLecture();

    @Query("SELECT COUNT(*) FROM calendar LIMIT 1")
    Cursor hasLectures();

    @Query("SELECT DISTINCT c.ROWID as _id, c.title, EXISTS (" +
           "SELECT * FROM widgets_timetable_blacklist WHERE widget_id=:widgetId AND lecture_title=c.title" +
           ") as is_on_blacklist from calendar c GROUP BY c.title")
    Cursor getBlacklistedLectures(String widgetId);

    @Query("DELETE FROM calendar")
    void flush();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Calendar cal);

    @Query("SELECT c.location " +
           "FROM calendar c LEFT JOIN room_locations r ON " +
           "c.location=r.title " +
           "WHERE r.latitude IS NULL " +
           "GROUP BY c.location")
    Cursor getLecturesWithoutCoordinates();

    @Query("SELECT title, dtstart, dtend, location FROM calendar JOIN " +
           "(SELECT dtstart AS maxstart FROM calendar WHERE status!='CANCEL' AND datetime('now', 'localtime')<dtstart " +
           "ORDER BY dtstart LIMIT 1) ON status!='CANCEL' AND datetime('now', 'localtime')<dtend AND dtstart<=maxstart " +
           "ORDER BY dtend, dtstart LIMIT 4")
    Cursor getNextCalendarItem();

    @Query("SELECT * " +
           "FROM calendar c, room_locations r " +
           "WHERE datetime('now', 'localtime') < datetime(c.dtstart, '+1800 seconds') AND " +
           "datetime('now','localtime') < c.dtend AND r.title == c.location AND c.status!='CANCEL'" +
           "ORDER BY dtstart LIMIT 1")
    Cursor getNextLectureCoordinates();
}
