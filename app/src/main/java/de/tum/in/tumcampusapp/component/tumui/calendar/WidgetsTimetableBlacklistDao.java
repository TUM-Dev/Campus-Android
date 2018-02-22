package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

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