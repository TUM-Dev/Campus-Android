package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.cafeteria.Cafeteria;
import io.reactivex.Flowable;

@Dao
public interface CafeteriaDao {
    @Query("SELECT * FROM cafeteria")
    Flowable<List<Cafeteria>> getAll();

    @Query("SELECT * FROM cafeteria WHERE id = :id")
    Flowable<Cafeteria> getById(int id);

    @Query("DELETE FROM cafeteria")
    void removeCache();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Cafeteria cafeteria);

    @Query("SELECT name FROM cafeteria WHERE id = :id")
    String getMensaNameFromId(int id);
}
