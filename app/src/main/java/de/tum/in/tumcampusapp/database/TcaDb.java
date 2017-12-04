package de.tum.in.tumcampusapp.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.database.dataAccessObjects.BuildingToGpsDao;
import de.tum.in.tumcampusapp.database.dataAccessObjects.CafeteriaDao;
import de.tum.in.tumcampusapp.database.dataAccessObjects.CafeteriaMenuDao;
import de.tum.in.tumcampusapp.database.dataAccessObjects.ChatMessageDao;
import de.tum.in.tumcampusapp.database.dataAccessObjects.UnreadChatMessageDao;
import de.tum.in.tumcampusapp.database.dataAccessObjects.FavoriteDishDao;
import de.tum.in.tumcampusapp.database.dataAccessObjects.KinoDao;
import de.tum.in.tumcampusapp.database.dataAccessObjects.LocationDao;
import de.tum.in.tumcampusapp.database.dataAccessObjects.SyncDao;
import de.tum.in.tumcampusapp.database.dataAccessObjects.TumLockDao;
import de.tum.in.tumcampusapp.database.dataAccessObjects.CalendarDao;
import de.tum.in.tumcampusapp.database.dataAccessObjects.WidgetTimetableBlacklistDao;
import de.tum.in.tumcampusapp.database.dataAccessObjects.RoomLocationsDao;


import de.tum.in.tumcampusapp.models.calendar.Calendar;
import de.tum.in.tumcampusapp.models.calendar.RoomLocations;
import de.tum.in.tumcampusapp.models.calendar.WidgetsTimetableBlacklist;

import de.tum.in.tumcampusapp.models.chat.ChatMessageTable;
import de.tum.in.tumcampusapp.models.chat.UnsentChatMessageTable;

import de.tum.in.tumcampusapp.models.cafeteria.Cafeteria;
import de.tum.in.tumcampusapp.models.cafeteria.CafeteriaMenu;
import de.tum.in.tumcampusapp.models.cafeteria.FavoriteDish;
import de.tum.in.tumcampusapp.models.cafeteria.Location;
import de.tum.in.tumcampusapp.models.dbEntities.Sync;
import de.tum.in.tumcampusapp.models.dbEntities.TumLock;
import de.tum.in.tumcampusapp.models.tumcabe.BuildingToGps;
import de.tum.in.tumcampusapp.models.tumcabe.Kino;

@Database(version = 1, entities = {
        Cafeteria.class,
        CafeteriaMenu.class,
        FavoriteDish.class,
        Sync.class,
        TumLock.class,
        BuildingToGps.class,
        Kino.class,
        Location.class,
        ChatMessageTable.class,
        UnsentChatMessageTable.class,
        Calendar.class,
        RoomLocations.class,
        WidgetsTimetableBlacklist.class
}, exportSchema = false) // TODO: probably version schema
@TypeConverters(Converters.class)
public abstract class TcaDb extends RoomDatabase {
    public abstract CafeteriaDao cafeteriaDao();

    public abstract CafeteriaMenuDao cafeteriaMenuDao();

    public abstract FavoriteDishDao favoriteDishDao();

    public abstract TumLockDao tumLockDao();

    public abstract SyncDao syncDao();

    public abstract BuildingToGpsDao buildingToGpsDao();

    public abstract KinoDao kinoDao();

    public abstract LocationDao locationDao();

    public abstract ChatMessageDao chatMessageDao();

    public abstract UnreadChatMessageDao unreadChatMessageDao();

    public abstract CalendarDao calendarDao();

    public abstract WidgetTimetableBlacklistDao widgetTimetableBlacklistDao();

    public abstract RoomLocationsDao roomLocationsDao();

    private static TcaDb instance;

    public static synchronized TcaDb getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), TcaDb.class, Const.DATABASE_NAME)
                           .build();
        }
        return instance;
    }
}
