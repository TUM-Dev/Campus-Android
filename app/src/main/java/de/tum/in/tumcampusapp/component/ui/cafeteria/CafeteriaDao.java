package de.tum.in.tumcampusapp.component.ui.cafeteria;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.annotation.Nullable;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.cafeteria.model.Cafeteria;
import io.reactivex.Flowable;

@Dao
public interface CafeteriaDao {
    @Query("SELECT * FROM cafeteria")
    Flowable<List<Cafeteria>> getAll();

    @Nullable
    @Query("SELECT * FROM cafeteria WHERE id = :id")
    Cafeteria getById(int id);

    @Query("DELETE FROM cafeteria")
    void removeCache();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Cafeteria cafeteria);

    @Query("SELECT name FROM cafeteria WHERE id = :id")
    String getMensaNameFromId(int id);
}
