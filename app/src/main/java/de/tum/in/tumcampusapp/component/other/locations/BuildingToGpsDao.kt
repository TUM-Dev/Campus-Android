package de.tum.`in`.tumcampusapp.component.other.locations

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.tum.`in`.tumcampusapp.component.other.locations.model.BuildingToGps

@Dao
interface BuildingToGpsDao {

    @Query("SELECT * FROM buildingtogps")
    fun getAll(): List<BuildingToGps>

    @Insert
    fun insert(buildingToGps: BuildingToGps)

    @Query("DELETE FROM buildingtogps")
    fun removeCache()

}
