package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import de.tum.in.tumcampusapp.models.dbEntities.OpenQuestions;

@Dao
public interface OpenQuestionsDao {

    @Query("SELECT question FROM openQuestions")
    Cursor getAll();

    @Query("DELETE FROM openQuestions WHERE question=:id")
    void deleteQuestionById(int id);

    @Query("SELECT * FROM openQuestions WHERE question=:id")
    Cursor getQuestionById(int id);

    @Insert
    void insert(OpenQuestions openQuestion);

    @Query("SELECT question, text FROM openQuestions WHERE answered=0 AND `end` >= :date")
    Cursor getUnansweredQuestions(String date);

    @Query("UPDATE openQuestions SET answered=1, answerid=:answer WHERE question=:id")
    void updateAnswer(int id, int answer);

    @Query("UPDATE openQuestions SET synced=1, answered=1 WHERE question=:id")
    void skipQuestion(int id);

    @Query("UPDATE openQuestions SET synced=1 WHERE question=:id")
    void markSynced(int id);

    @Query("SELECT question, answerid FROM openQuestions WHERE synced=0 AND answered=1")
    Cursor getAnsweredNotSynced();

    @Query("DELETE FROM openQuestions")
    void flush();
}
