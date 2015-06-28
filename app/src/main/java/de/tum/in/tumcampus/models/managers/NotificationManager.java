package de.tum.in.tumcampus.models.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import de.tum.in.tumcampus.models.Notification;
import de.tum.in.tumcampus.models.NotificationLocation;
import de.tum.in.tumcampus.models.NotificationType;
import de.tum.in.tumcampus.models.TUMCabeClient;

public class NotificationManager {
    private static final String TABLE_TYPES = "notification_types";
    private static final String[] TABLE_TYPES_COLUMNS
            = new String[]{"type", "name", "icon", "silent", "confirmation"};

    private enum typesColumns {
        id, name, icon, silent, confirmation
    }

    private static final String TABLE_NOTIFICATIONS = "notification_alarm";
    private static final String[] TABLE_NOTIFICATIONS_COLUMNS = new String[]{
            "notification", "type", "location", "name", "lon", "lat", "rad", "title",
            "desc", "signature"};

    private enum alarmColumns {
        id, type, location, locationName, lon, lat, rad, title, desc, signature
    }

    private final SQLiteDatabase db;

    private final Context context;

    public NotificationManager(Context context) {
        this.context = context;
        db = DatabaseManager.getDb(context);

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TYPES +
                "(type INTEGER, name VARCHAR, icon VARCHAR, silent INTEGER, confirmation INTEGER, " +
                "PRIMARY KEY type)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATIONS +
                "(notification INTEGER, type INTEGER, location INTEGER, name VARCHAR, lon REAL, " +
                "lat REAL, radius INTEGER, title VARCHAR, desc VARCHAR, signature VARCHAR, " +
                "PRIMARY KEY notification)");
    }

    public void replaceInto(NotificationType type) {
        ContentValues cvs = new ContentValues();
        cvs.put(TABLE_TYPES_COLUMNS[typesColumns.id.ordinal()], type.getType());
        cvs.put(TABLE_TYPES_COLUMNS[typesColumns.name.ordinal()], type.getName());
        cvs.put(TABLE_TYPES_COLUMNS[typesColumns.icon.ordinal()], type.getIcon());
        cvs.put(TABLE_TYPES_COLUMNS[typesColumns.silent.ordinal()], type.isSilent());
        cvs.put(TABLE_TYPES_COLUMNS[typesColumns.confirmation.ordinal()], type.isConfirmation());
        db.insertWithOnConflict(TABLE_TYPES, null, cvs, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void replaceTypesInto(List<NotificationType> types) {
        for (NotificationType type : types) {
            this.replaceInto(type);
        }
    }

    public void replaceInto(Notification note) {
        ContentValues cvs = new ContentValues();
        cvs.put("notification", note.getNotification());
        cvs.put("type", note.getType());
        cvs.put("location", note.getDescription());
        cvs.put("name", note.getLocation().getName());
        cvs.put("lon", note.getLocation().getLon());
        cvs.put("lat", note.getLocation().getLat());
        cvs.put("title", note.getTitle());
        cvs.put("desc", note.getDescription());
        cvs.put("signature", note.getSignature());
        db.insertWithOnConflict(TABLE_NOTIFICATIONS, null, cvs, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void replaceNotificationsInto(List<Notification> notes) {
        for (Notification note : notes) {
            this.replaceInto(note);
        }
    }

    public Notification getNotification(int notificationId) {
        Cursor c = db.query(TABLE_NOTIFICATIONS, TABLE_NOTIFICATIONS_COLUMNS, "notification = ?",
                new String[]{Integer.toString(notificationId)}, null, null, null);
        c.moveToFirst();
        Notification n;
        if(c.getCount() == 0){
            //update cache
            List<Notification> notes = TUMCabeClient.getInstance(context).getNotifications(notificationId);
            this.replaceNotificationsInto(notes);
            for(Notification note : notes) {
                if(note.getNotification() == notificationId) {
                    n = note;
                    break;
                }
            }
            n = null;
        } else {
            n = new Notification(c.getInt(alarmColumns.id.ordinal()),
                    c.getInt(alarmColumns.type.ordinal()),
                    new NotificationLocation(
                            c.getInt(alarmColumns.location.ordinal()),
                            c.getString(alarmColumns.locationName.ordinal()),
                            c.getDouble(alarmColumns.lon.ordinal()),
                            c.getDouble(alarmColumns.lat.ordinal()),
                            c.getInt(alarmColumns.rad.ordinal())),
                    c.getString(alarmColumns.title.ordinal()),
                    c.getString(alarmColumns.desc.ordinal()),
                    c.getString(alarmColumns.signature.ordinal()));
        }
        c.close();
        return n;
    }

    public NotificationType getType(int type) {
        Cursor c = db.query(TABLE_TYPES, TABLE_TYPES_COLUMNS,
                "id = ?", new String[]{Integer.toString(type)}, null, null, null);
        c.moveToFirst();
        NotificationType t = new NotificationType(c.getInt(typesColumns.id.ordinal()),
                c.getString(typesColumns.name.ordinal()),
                c.getString(typesColumns.icon.ordinal()),
                c.getInt(typesColumns.silent.ordinal()) == 1,
                c.getInt(typesColumns.confirmation.ordinal()) == 1);
        c.close();
        return t;
    }
}
