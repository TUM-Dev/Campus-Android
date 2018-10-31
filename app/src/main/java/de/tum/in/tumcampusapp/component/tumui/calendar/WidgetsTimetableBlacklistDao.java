package de.tum.in.tumcampusapp.component.tumui.calendar;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import de.tum.in.tumcampusapp.component.tumui.calendar.model.WidgetsTimetableBlacklist;

@Dao
public interface WidgetsTimetableBlacklistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WidgetsTimetableBlacklist widgetsTimetableBlacklist);

    @Delete
    void delete(WidgetsTimetableBlacklist widgetsTimetableBlacklist);

    @Query("DELETE FROM widgets_timetable_blacklist")
    void flush();
}