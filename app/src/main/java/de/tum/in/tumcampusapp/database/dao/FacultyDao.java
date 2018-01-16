package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.tumcabe.Faculty;

@Dao
public interface FacultyDao {

    @Query("DELETE FROM faculty")
    void flush();
}
