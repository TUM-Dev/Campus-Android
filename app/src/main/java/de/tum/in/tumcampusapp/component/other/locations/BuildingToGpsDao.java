package de.tum.in.tumcampusapp.component.other.locations;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import de.tum.in.tumcampusapp.component.other.locations.model.BuildingToGps;

@Dao
public interface BuildingToGpsDao {
    @Insert
    void insert(BuildingToGps... buildingToGps);

    @Query("SELECT * FROM buildingtogps")
    List<BuildingToGps> getAll();

    @Query("DELETE FROM buildingtogps")
    void removeCache();
}
