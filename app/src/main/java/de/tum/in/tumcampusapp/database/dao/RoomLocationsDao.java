package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import de.tum.in.tumcampusapp.models.dbEntities.RoomLocations;

@Dao
public interface RoomLocationsDao {
    @Query("SELECT * " +
           "FROM calendar c, room_locations r " +
           "WHERE datetime('now', 'localtime') < datetime(c.dtstart, '+1800 seconds') AND " +
           "datetime('now','localtime') < c.dtend AND r.title == c.location AND c.status!='CANCEL'" +
           "ORDER BY dtstart LIMIT 1")
    RoomLocations getNextLectureCoordinates();

    @Insert
    void insert(RoomLocations roomLocations);
}