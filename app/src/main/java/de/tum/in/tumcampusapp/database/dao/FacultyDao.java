package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import de.tum.in.tumcampusapp.models.tumcabe.Faculty;

@Dao
public interface FacultyDao {
    @Query("SELECT * FROM faculties")
    Cursor getAll();

    @Query("SELECT faculty FROM faculties WHERE name=:name")
    Cursor getFacultyIdByName(String name);

    @Query("SELECT name FROM faculties WHERE faculty=:id")
    Cursor getFacultyNameById(String id);

    @Query("SELECT * FROM faculties WHERE faculty=:id")
    Cursor getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Faculty faculty);

    @Query("DELETE FROM faculties")
    void flush();
}
