package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.dbEntities.OpenQuestions;

@Dao
public interface OpenQuestionsDao {

    @Query("DELETE FROM openQuestions")
    void flush();
}
