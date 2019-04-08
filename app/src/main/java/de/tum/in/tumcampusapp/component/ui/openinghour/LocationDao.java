package de.tum.in.tumcampusapp.component.ui.openinghour;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import de.tum.in.tumcampusapp.component.ui.openinghour.model.Location;

@Dao
public interface LocationDao {

    @Query("SELECT hours FROM location WHERE id = :id")
    String getHoursById(int id);

    @Query("SELECT * FROM location WHERE category = :category ORDER BY name")
    List<Location> getAllOfCategory(String category);

    @Query("SELECT NOT count(*) FROM location")
    boolean isEmpty();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void replaceInto(List<Location> location);

    @Query("DELETE FROM location")
    void removeCache();
}
