package de.tum.in.tumcampusapp.database.dataAccessObjects;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.tumcabe.News;

@Dao
public interface NewsDao {

    @Query("DELETE FROM news WHERE date < date('now','-3 month')")
    void cleanUp();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(News news);
}
