package de.tum.`in`.tumcampusapp.component.notifications.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface ActiveAlarmsDao {

    @Insert
    fun addActiveAlarm(alarm: ActiveAlarm)

    @Delete
    fun deleteActiveAlarm(alarm: ActiveAlarm)

    @Query("SELECT CASE WHEN count(*) < $MAX_ACTIVE THEN $MAX_ACTIVE - count(*) ELSE 0 END FROM active_alarms")
    fun maxAlarmsToSchedule(): Int

    companion object {
        private const val MAX_ACTIVE = 100
    }
}