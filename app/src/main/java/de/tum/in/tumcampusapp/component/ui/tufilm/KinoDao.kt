package de.tum.`in`.tumcampusapp.component.ui.tufilm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import io.reactivex.Flowable

@Dao
interface KinoDao {

    @get:Query("SELECT * FROM kino ORDER BY date")
    val all: Flowable<List<Kino>>

    @get:Query("SELECT id FROM kino ORDER BY date DESC LIMIT 1")
    val latestId: String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg kino: Kino)

    @Query("SELECT count(*) FROM kino WHERE date < :date ORDER BY date DESC")
    fun getPositionByDate(date: String): Int

    // Using the id directly does not work since it is stored as a string and it is therefore not ordered properly
    @Query("SELECT count(*) FROM kino WHERE date < (Select date FROM kino WHERE id = :id LIMIT 1) ORDER BY date DESC")
    fun getPositionById(id: String): Int

    @Query("SELECT * FROM kino ORDER BY date LIMIT 1 OFFSET :position")
    fun getByPosition(position: Int): Flowable<Kino>

    @Query("DELETE FROM kino")
    fun flush()
}
