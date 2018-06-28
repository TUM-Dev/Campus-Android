package de.tum.in.tumcampusapp.component.other.general;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import javax.annotation.Nullable;

import de.tum.in.tumcampusapp.component.ui.alarm.model.FcmNotification;

@Dao
public interface NotificationDao {
    @Nullable
    @Query("SELECT * FROM notification n WHERE n.notification = :notificationId")
    FcmNotification get(int notificationId);

    @Query("DELETE FROM notification")
    void cleanup();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FcmNotification gcmNotification);
}
