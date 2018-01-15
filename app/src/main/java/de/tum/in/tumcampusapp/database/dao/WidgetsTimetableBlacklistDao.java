package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import java.util.List;

import de.tum.in.tumcampusapp.models.dbEntities.WidgetsTimetableBlacklist;

@Dao
public interface WidgetsTimetableBlacklistDao  {
    @Query("SELECT * FROM widgets_timetable_blacklist")
    List<WidgetsTimetableBlacklist> hi();
}