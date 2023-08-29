package de.tum.`in`.tumcampusapp.component.notifications.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ActiveAlarmsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addActiveAlarm(alarm: ActiveAlarm)

    @Delete
    fun deleteActiveAlarm(alarm: ActiveAlarm)

    @Query("SELECT CASE WHEN count(*) < $MAX_ACTIVE THEN $MAX_ACTIVE - count(*) ELSE 0 END FROM active_alarms")
    fun maxAlarmsToSchedule(): Int

    @Query("SELECT * FROM active_alarms")
    fun getAllAlarms(): List<ActiveAlarm>

    companion object {
        private const val MAX_ACTIVE = 100
    }
}
