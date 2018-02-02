package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import javax.annotation.Nullable;

import de.tum.in.tumcampusapp.models.dbEntities.OpenQuestions;

@Dao
public interface OpenQuestionsDao {

    @Query("SELECT * FROM openQuestions")
    List<OpenQuestions> getAll();

    @Query("DELETE FROM openQuestions WHERE question=:id")
    void deleteQuestionById(int id);

    @Nullable
    @Query("SELECT * FROM openQuestions WHERE question=:id")
    OpenQuestions getQuestionById(int id);

    @Insert
    void insert(OpenQuestions openQuestion);

    @Query("SELECT * FROM openQuestions WHERE answered=0 AND `end` >= :date")
    List<OpenQuestions> getUnansweredQuestions(String date);

    @Query("UPDATE openQuestions SET synced=1 WHERE question=:id")
    void markSynced(int id);

    @Query("SELECT * FROM openQuestions WHERE synced=0 AND answered=1")
    List<OpenQuestions> getAnsweredNotSynced();

    @Query("DELETE FROM openQuestions")
    void flush();
}
