package de.tum.in.tumcampusapp.api.tumonline;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import javax.annotation.Nullable;

import de.tum.in.tumcampusapp.api.tumonline.model.TumLock;

@Dao
public interface TumLockDao {
    @Query("DELETE FROM tumLock WHERE datetime() > datetime(strftime('%s',timestamp) + " + TumManager.MAX_AGE + ", 'unixepoch') AND active=0")
    void deleteObsolete();

    @Query("UPDATE tumLock SET active=0 WHERE datetime() > datetime(strftime('%s',timestamp) + lockedFor, 'unixepoch') AND active=1")
    void deactivateExpired();

    @Update
    void releaseLock(TumLock lock);

    @Nullable
    @Query("SELECT * FROM tumLock WHERE url = :url")
    TumLock getFromUrl(String url);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setLock(TumLock lock);

    @Query("DELETE FROM tumLock")
    void removeCache();
}
