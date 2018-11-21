package de.tum.in.tumcampusapp.component.ui.tufilm;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import io.reactivex.Flowable;

@Dao
public interface KinoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Kino kino);

    @Query("SELECT * FROM kino ORDER BY date")
    Flowable<List<Kino>> getAll();

    @Query("SELECT id FROM kino ORDER BY id DESC LIMIT 1")
    String getLatestId();

    @Query("SELECT count(*) FROM kino WHERE date < :date ORDER BY date DESC")
    int getPositionByDate(String date);

    @Query("SELECT count(*) FROM kino WHERE id < :id ORDER BY date DESC")
    int getPositionById(String id);

    @Query("SELECT * FROM kino ORDER BY date LIMIT 1 OFFSET :position")
    Flowable<Kino> getByPosition(int position);

    @Query("DELETE FROM kino")
    void flush();
}
