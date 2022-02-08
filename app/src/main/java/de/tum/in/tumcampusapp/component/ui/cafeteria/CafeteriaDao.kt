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

    /**
     * Used for fetching the actual primary key 'id' from the table via the CafeteriaLocation enum and the cafeteriaId embedded in that enum.
     *
     * @param cafeteriaSlug the cafeteria for which dishes should be fetched as valid cafeteria slug e.g.: "mensa-garching"
     * @return the 'id' corresponding to the provided cafeteriaId
     */
    @Query("SELECT id FROM cafeteria WHERE slug = :cafeteriaSlug")
    fun getIdFrom(cafeteriaSlug: String): Int
}
