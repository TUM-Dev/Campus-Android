package de.tum.in.tumcampusapp.component.other.general;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import de.tum.in.tumcampusapp.component.other.general.model.Recent;

@Dao
public interface RecentsDao {
    int STATIONS = 1;
//    int ROOMS = 2; legacy room
    int PERSONS = 3;
    int LECTURES = 4;
    int NAVIGATUM_ROOMS = 5;
    int NAVIGATUM_BUILDINGS = 6;

    @Nullable
    @Query("SELECT * FROM recent WHERE type=:type")
    List<Recent> getAll(Integer type);

    @Query("SELECT * FROM recent")
    List<Recent> getAllRecentSearches();

    @Query("DELETE FROM recent WHERE name=:name")
    void deleteByName(String name);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Recent recent);

    @Query("DELETE FROM recent")
    void removeCache();
}
