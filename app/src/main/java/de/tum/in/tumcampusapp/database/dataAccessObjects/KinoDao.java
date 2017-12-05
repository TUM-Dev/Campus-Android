package de.tum.in.tumcampusapp.database.dataAccessObjects;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.tumcabe.Kino;

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
    List<Kino> getAll();

    @Query("SELECT id FROM kino ORDER BY id DESC LIMIT 1")
    String getLastId();

    @Query("SELECT * FROM kino ORDER BY id LIMIT 1 OFFSET :position")
    Kino getByPosition(int position);

    @Query("DELETE FROM kino")
    void flush();
}
