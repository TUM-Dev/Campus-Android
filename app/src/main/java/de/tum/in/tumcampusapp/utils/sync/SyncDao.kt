package de.tum.`in`.tumcampusapp.utils.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tum.`in`.tumcampusapp.utils.sync.model.Sync

@Dao
interface SyncDao {
    @Query("SELECT lastSync FROM sync WHERE (strftime('%s','now') - strftime('%s',lastSync)) < :seconds AND id=:id")
    fun getSyncSince(id: String, seconds: Int): String?

    @Query("DELETE FROM sync")
    fun removeCache()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sync: Sync)
}
