package de.tum.`in`.tumcampusapp.component.tumui.calendar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import org.joda.time.DateTime

@Dao
interface CalendarDao {
    @get:Query("SELECT c.* FROM calendar c WHERE status != 'CANCEL'")
    val allNotCancelled: List<CalendarItem>

    @get:Query("SELECT c.* FROM calendar c WHERE datetime('now', 'localtime') BETWEEN dtstart AND dtend AND status != 'CANCEL' ORDER BY title")
    val currentLectures: List<CalendarItem>

    @get:Query("SELECT c.* " +
            "FROM calendar c LEFT JOIN room_locations r ON " +
            "c.location=r.title " +
            "WHERE coalesce(r.latitude, '') = '' " +
            "GROUP BY c.location")
    val lecturesWithoutCoordinates: List<CalendarItem>

    @get:Query("SELECT c.* FROM calendar c JOIN " +
            "(SELECT dtstart AS maxstart FROM calendar WHERE status!='CANCEL' AND datetime('now', 'localtime')<dtstart " +
            "ORDER BY dtstart LIMIT 1) ON status!='CANCEL' AND datetime('now', 'localtime')<dtend AND dtstart<=maxstart " +
            "ORDER BY dtend, dtstart LIMIT 4")
    val nextCalendarItems: List<CalendarItem>

    @get:Query("SELECT * FROM calendar " +
            "WHERE status!='CANCEL' " +
            "AND dtstart > datetime('now', 'localtime') " +
            "GROUP BY title, dtstart, dtend " +
            "ORDER BY dtstart LIMIT 4")
    val nextUniqueCalendarItems: List<CalendarItem>

    @Query("SELECT c.* FROM calendar c WHERE dtstart LIKE '%' || :date || '%' ORDER BY dtstart ASC")
    fun getAllByDate(date: DateTime): List<CalendarItem>

    @Query("SELECT c.* FROM calendar c WHERE dtend BETWEEN :from AND :to " + "ORDER BY dtstart, title, location ASC")
    fun getAllBetweenDates(from: DateTime, to: DateTime): List<CalendarItem>

    @Query("SELECT c.* FROM calendar c WHERE dtend BETWEEN :from AND :to " +
            "AND STATUS != 'CANCEL'" +
            "ORDER BY dtstart, title, location ASC")
    fun getAllNotCancelledBetweenDates(from: DateTime, to: DateTime): List<CalendarItem>

    @Query("SELECT c.* FROM calendar c WHERE dtend BETWEEN :from AND :to " +
            "AND STATUS != 'CANCEL'" +
            "AND NOT EXISTS (SELECT * FROM widgets_timetable_blacklist WHERE widget_id = :widgetId" +
            "                AND lecture_title = c.title)" +
            "ORDER BY dtstart ASC")
    fun getNextDays(from: DateTime, to: DateTime, widgetId: String): List<CalendarItem>

    @Query("SELECT COUNT(*) FROM calendar")
    fun hasLectures(): Boolean

    @Query("SELECT c.* FROM calendar c, widgets_timetable_blacklist " +
            "WHERE widget_id=:widgetId AND lecture_title=title " +
            "GROUP BY title")
    fun getLecturesInBlacklist(widgetId: String): List<CalendarItem>

    @Query("SELECT c.* FROM calendar c " +
            "WHERE NOT EXISTS (SELECT * FROM widgets_timetable_blacklist " +
            "WHERE widget_id=:widgetId AND c.title=lecture_title) " +
            "GROUP BY c.title")
    fun getLecturesNotInBlacklist(widgetId: String): List<CalendarItem>

    @Query("DELETE FROM calendar")
    fun flush()

    @Query("DELETE FROM calendar WHERE nr=:eventNr")
    fun delete(eventNr: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg cal: CalendarItem)

    @Query("SELECT location FROM calendar " +
            "WHERE title = (SELECT title FROM calendar WHERE nr=:id) " +
            "AND dtstart = (SELECT dtstart FROM calendar WHERE nr=:id) " +
            "AND dtend = (SELECT dtend FROM calendar WHERE nr=:id) " +
            "AND status != 'CANCEL' " +
            "ORDER BY location ASC")
    fun getNonCancelledLocationsById(id: String): List<String>

    @Query("SELECT * FROM calendar WHERE nr=:id" +
            " UNION " +
            "SELECT * FROM calendar " +
            "WHERE title = (SELECT title FROM calendar WHERE nr=:id) " +
            "AND dtstart = (SELECT dtstart FROM calendar WHERE nr=:id) " +
            "AND dtend = (SELECT dtend FROM calendar WHERE nr=:id) " +
            "AND nr != :id " +
            "ORDER BY location ASC")
    fun getCalendarItemsById(id: String): List<CalendarItem>

    @Query("SELECT * FROM calendar WHERE nr=:id")
    fun getCalendarItemById(id: String): CalendarItem
}
