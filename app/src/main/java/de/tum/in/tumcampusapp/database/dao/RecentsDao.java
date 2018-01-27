package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import javax.annotation.Nullable;

import de.tum.in.tumcampusapp.models.dbEntities.Recent;

@Dao
public interface RecentsDao {
    @Nullable
    @Query("SELECT * FROM recent WHERE type=:type")
    List<Recent> getAll(Integer type);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Recent recent);

    @Query("DELETE FROM recent")
    void removeCache();
}
