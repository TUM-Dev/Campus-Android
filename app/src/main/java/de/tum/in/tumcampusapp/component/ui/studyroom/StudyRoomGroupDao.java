package de.tum.in.tumcampusapp.component.ui.studyroom;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup;

@Dao
public interface StudyRoomGroupDao {

    @Query("SELECT * FROM study_room_groups")
    List<StudyRoomGroup> getAll();

    @Query("DELETE FROM study_room_groups")
    void removeCache();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StudyRoomGroup studyRoomGroup);

}
