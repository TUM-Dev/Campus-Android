package de.tum.in.tumcampusapp.component.other.general;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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
