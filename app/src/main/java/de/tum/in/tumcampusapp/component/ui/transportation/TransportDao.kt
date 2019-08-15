package de.tum.`in`.tumcampusapp.component.ui.transportation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import de.tum.`in`.tumcampusapp.component.ui.transportation.model.TransportFavorites
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.WidgetsTransport

@Dao
interface TransportDao {

    @Query("SELECT EXISTS(SELECT * FROM transport_favorites WHERE symbol = :symbol)")
    fun isFavorite(symbol: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFavorite(transportFavorites: TransportFavorites)

    @Query("DELETE FROM transport_favorites WHERE symbol = :symbol")
    fun deleteFavorite(symbol: String)

    @Query("SELECT * FROM widgets_transport WHERE id = :id")
    fun getAllWithId(id: Int): WidgetsTransport?

    @Query("DELETE FROM widgets_transport WHERE id = :id")
    fun deleteWidget(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun replaceWidget(widgetsTransport: WidgetsTransport)

    @Query("DELETE FROM transport_favorites")
    fun removeCache()
}
