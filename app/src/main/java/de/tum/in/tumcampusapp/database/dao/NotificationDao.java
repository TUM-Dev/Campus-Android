package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import javax.annotation.Nullable;

import de.tum.in.tumcampusapp.models.gcm.GCMNotification;

@Dao
public interface NotificationDao {
    @Nullable
    @Query("SELECT * FROM notification n WHERE n.notification = :notificationId")
    GCMNotification get(int notificationId);

    @Query("DELETE FROM notification")
    void cleanup();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GCMNotification gcmNotification);
}
