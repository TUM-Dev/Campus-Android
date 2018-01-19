package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.cafeteria.Cafeteria;
import de.tum.in.tumcampusapp.models.tumcabe.StudyRoom;
import de.tum.in.tumcampusapp.models.tumcabe.StudyRoomGroup;

@Dao
public interface StudyRoomDao {

    @Query("SELECT id, code, name, location, occupied_till, group_id FROM study_rooms WHERE group_id =:groupId ORDER BY occupied_till ASC")
    List<StudyRoom> getAll(int groupId);

    @Query("DELETE FROM study_rooms")
    void removeCache();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StudyRoom studyRoom);

}
