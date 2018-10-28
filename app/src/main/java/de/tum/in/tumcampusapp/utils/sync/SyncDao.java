package de.tum.in.tumcampusapp.utils.sync;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import javax.annotation.Nullable;

import de.tum.in.tumcampusapp.utils.sync.model.Sync;

@Dao
public interface SyncDao {
    @Nullable
    @Query("SELECT lastSync FROM sync WHERE (strftime('%s','now') - strftime('%s',lastSync)) < :seconds AND id=:id")
    String getSyncSince(String id, int seconds);

    @Query("DELETE FROM sync")
    void removeCache();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Sync sync);
}
