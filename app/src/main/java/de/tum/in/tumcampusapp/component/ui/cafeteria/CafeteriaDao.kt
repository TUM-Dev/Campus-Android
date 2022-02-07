package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import io.reactivex.Flowable

@Dao
interface CafeteriaDao {
    @get:Query("SELECT * FROM cafeteria")
    val all: Flowable<List<Cafeteria>>

    /**
     * @param cafeteriaId A valid cafeteriaId, generated via the CafeteriaLocation enum.
     */
    @Query("SELECT * FROM cafeteria WHERE id = :cafeteriaId")
    fun getById(cafeteriaId: Int): Cafeteria?

    @Query("DELETE FROM cafeteria")
    fun removeCache()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cafeterias: List<Cafeteria>)

    /**
     * @param cafeteriaId A valid cafeteriaId, generated via the CafeteriaLocation enum.
     */
    @Query("SELECT name FROM cafeteria WHERE id = :cafeteriaId")
    fun getMensaNameFromId(cafeteriaId: Int): String
}
