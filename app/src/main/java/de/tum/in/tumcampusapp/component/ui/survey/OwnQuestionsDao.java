package de.tum.in.tumcampusapp.component.ui.survey;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.survey.model.OwnQuestions;

@Dao
public interface OwnQuestionsDao {

    @Query("SELECT * FROM ownQuestions where deleted = 0 AND `end` >= :date")
    List<OwnQuestions> getFromEndDate(String date);

    @Query("SELECT created FROM ownQuestions WHERE created >= :date")
    List<String> getFromCreatedDate(String date);

    @Query("UPDATE ownQuestions SET deleted=1 WHERE question=:id")
    void setDeletedById(int id);

    @Query("SELECT * FROM ownQuestions WHERE question=:id")
    OwnQuestions getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OwnQuestions question);

    @Query("DELETE FROM ownQuestions")
    void flush();
}
