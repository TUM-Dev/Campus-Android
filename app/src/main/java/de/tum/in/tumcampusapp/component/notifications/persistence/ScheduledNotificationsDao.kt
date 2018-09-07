package de.tum.`in`.tumcampusapp.component.notifications.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface ScheduledNotificationsDao {

    @Query("SELECT * FROM scheduled_notifications WHERE type_id = :typeId AND content_id = :contentId LIMIT 1")
    fun find(typeId: Int, contentId: Int): ScheduledNotification?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(notification: ScheduledNotification): Long

    @Query("DELETE FROM scheduled_notifications WHERE type_id = :typeId AND content_id = :contentId")
    fun delete(typeId: Int, contentId: Int)

}