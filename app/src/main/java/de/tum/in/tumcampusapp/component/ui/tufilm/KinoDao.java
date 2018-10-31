package de.tum.in.tumcampusapp.component.ui.tufilm;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import io.reactivex.Flowable;

@Dao
public interface KinoDao {

    /**
     * Removes all old items
     */
    @Query("DELETE FROM kino WHERE date < date('now')")
    void cleanUp();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Kino kino);

    @Query("SELECT * FROM kino ORDER BY date")
    Flowable<List<Kino>> getAll();

    @Query("SELECT id FROM kino ORDER BY id DESC LIMIT 1")
    String getLatestId();

    @Query("SELECT count(*) FROM kino WHERE date < :date")
    int getPosition(String date);

    @Query("SELECT * FROM kino ORDER BY date LIMIT 1 OFFSET :position")
    Flowable<Kino> getByPosition(int position);

    @Query("DELETE FROM kino")
    void flush();
}
