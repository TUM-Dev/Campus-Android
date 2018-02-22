package de.tum.in.tumcampusapp.component.ui.tufilm;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Dao
public interface KinoDao {

    /**
     * Removes all old items
     */
    @Query("DELETE FROM kino WHERE date < date('now')")
    void cleanUp();

    @Insert
    void insert(Kino kino);

    @Query("SELECT * FROM kino")
    Flowable<List<Kino>> getAll();

    @Query("SELECT id FROM kino ORDER BY id DESC LIMIT 1")
    Maybe<String> getLastId();

    @Query("SELECT * FROM kino ORDER BY id LIMIT 1 OFFSET :position")
    Flowable<Kino> getByPosition(int position);

    @Query("DELETE FROM kino")
    void flush();
}
