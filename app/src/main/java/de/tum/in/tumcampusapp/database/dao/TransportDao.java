package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.transport.TransportFavorites;
import de.tum.in.tumcampusapp.models.transport.WidgetsTransport;

@Dao
public interface TransportDao {

    @Query("SELECT EXISTS(SELECT * FROM transport_favorites WHERE symbol = :symbol)")
    boolean isFavorite(String symbol);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFavorite(TransportFavorites transportFavorites);

    @Query("DELETE FROM transport_favorites WHERE symbol = :symbol")
    void deleteFavorite(String symbol);

    @Query("SELECT * FROM widgets_transport WHERE id = :id")
    WidgetsTransport getAllWithId(int id);

    @Query("DELETE FROM widgets_transport WHERE id = :id")
    void deleteWidget(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void replaceWidget(WidgetsTransport widgetsTransport);
}
