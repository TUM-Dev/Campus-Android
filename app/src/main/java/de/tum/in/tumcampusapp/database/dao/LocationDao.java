package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.cafeteria.Location;

@Dao
public interface LocationDao {

    @Query("SELECT hours FROM location WHERE id = :id")
    String getHoursById(int id);

    @Query("SELECT * FROM location WHERE category = :category ORDER BY name")
    List<Location> getAllOfCategory(String category);

    @Query("SELECT NOT count(*) FROM location")
    boolean isEmpty();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void replaceInto(Location location);

    @Query("DELETE FROM location")
    void removeCache();
}
