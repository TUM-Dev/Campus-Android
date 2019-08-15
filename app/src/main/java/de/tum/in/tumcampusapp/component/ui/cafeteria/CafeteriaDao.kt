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

    @Query("SELECT * FROM cafeteria WHERE id = :id")
    fun getById(id: Int): Cafeteria?

    @Query("DELETE FROM cafeteria")
    fun removeCache()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cafeterias: List<Cafeteria>)

    @Query("SELECT name FROM cafeteria WHERE id = :id")
    fun getMensaNameFromId(id: Int): String
}
