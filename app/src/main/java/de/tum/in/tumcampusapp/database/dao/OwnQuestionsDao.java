package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import de.tum.in.tumcampusapp.models.dbEntities.OwnQuestions;

@Dao
public interface OwnQuestionsDao {

    @Query("SELECT * FROM ownQuestions where deleted = 0 AND `end` >= :date")
    Cursor getFromEndDate(String date);

    @Query("SELECT created FROM ownQuestions WHERE created >= :date")
    Cursor getFromCreatedDate(String date);

    @Query("UPDATE ownQuestions SET deleted=1 WHERE question=:id")
    void setDeletedById(int id);

    @Query("SELECT * FROM ownQuestions WHERE question=:id")
    Cursor getById(int id);

    @Query("DELETE FROM ownQuestions")
    void flush();
}
