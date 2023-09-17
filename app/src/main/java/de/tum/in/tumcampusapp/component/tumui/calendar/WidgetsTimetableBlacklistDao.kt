package de.tum.`in`.tumcampusapp.component.tumui.calendar

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.WidgetsTimetableBlacklist

@Dao
interface WidgetsTimetableBlacklistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(widgetsTimetableBlacklist: WidgetsTimetableBlacklist)

    @Delete
    fun delete(widgetsTimetableBlacklist: WidgetsTimetableBlacklist)

    @Query("DELETE FROM widgets_timetable_blacklist")
    fun flush()
}
