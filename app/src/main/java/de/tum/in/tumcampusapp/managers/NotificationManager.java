package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import java.util.List;

import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dao.NotificationDao;
import de.tum.in.tumcampusapp.models.gcm.GCMNotification;

public class NotificationManager {

    private final NotificationDao dao;

    public NotificationManager(Context context) {
        dao = TcaDb.getInstance(context)
                   .notificationDao();
    }

    public void replaceInto(GCMNotification notification) {
        dao.insert(notification);
    }

    public void replaceNotificationsInto(List<GCMNotification> notification) {
        for (GCMNotification note : notification) {
            this.replaceInto(note);
        }
    }

    public GCMNotification getNotification(int notificationId) {
        return dao.get(notificationId);
    }
}
