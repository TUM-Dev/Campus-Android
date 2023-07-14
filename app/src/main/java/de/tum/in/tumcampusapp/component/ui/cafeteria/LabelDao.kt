package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.Label

@Dao
interface LabelDao {

    @get:Query("SELECT DISTINCT * from labels")
    val allLabels: List<Label>

    @Query("DELETE FROM labels")
    fun removeCache()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(labels: List<Label>)
}