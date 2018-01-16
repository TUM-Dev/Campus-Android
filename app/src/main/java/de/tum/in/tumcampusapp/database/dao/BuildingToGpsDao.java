package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.tumcabe.BuildingToGps;

@Dao
public interface BuildingToGpsDao {
    @Insert
    void insert(BuildingToGps buildingToGps);

    @Query("SELECT * FROM buildingtogps")
    List<BuildingToGps> getAll();
}
