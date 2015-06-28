package de.tum.in.tumcampus.notifications;

import android.content.Context;
import android.os.Bundle;

import de.tum.in.tumcampus.models.Notification;
import de.tum.in.tumcampus.models.NotificationType;
import de.tum.in.tumcampus.models.managers.NotificationManager;

public class AlarmNotification {
    private final NotificationType type;
    private final Notification note;

    public AlarmNotification(Bundle extras, Context context) {
        NotificationManager man = new NotificationManager(context);
        type = man.getType(extras.getInt("notificationType"));
        note = man.getNotification(extras.getInt("notificationId"));
    }

    public Notification getAlarmNotification() {
        return null; //TODO
    }
}
