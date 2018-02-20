package de.tum.in.tumcampusapp.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.content.Intent;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.database.dao.BuildingToGpsDao;
import de.tum.in.tumcampusapp.database.dao.CafeteriaDao;
import de.tum.in.tumcampusapp.database.dao.CafeteriaMenuDao;
import de.tum.in.tumcampusapp.database.dao.CalendarDao;
import de.tum.in.tumcampusapp.database.dao.ChatMessageDao;
import de.tum.in.tumcampusapp.database.dao.ChatRoomDao;
import de.tum.in.tumcampusapp.database.dao.FacultyDao;
import de.tum.in.tumcampusapp.database.dao.FavoriteDishDao;
import de.tum.in.tumcampusapp.database.dao.KinoDao;
import de.tum.in.tumcampusapp.database.dao.LocationDao;
import de.tum.in.tumcampusapp.database.dao.NewsDao;
import de.tum.in.tumcampusapp.database.dao.NewsSourcesDao;
import de.tum.in.tumcampusapp.database.dao.NotificationDao;
import de.tum.in.tumcampusapp.database.dao.OpenQuestionsDao;
import de.tum.in.tumcampusapp.database.dao.OwnQuestionsDao;
import de.tum.in.tumcampusapp.database.dao.RecentsDao;
import de.tum.in.tumcampusapp.database.dao.RoomLocationsDao;
import de.tum.in.tumcampusapp.database.dao.StudyRoomDao;
import de.tum.in.tumcampusapp.database.dao.StudyRoomGroupDao;
import de.tum.in.tumcampusapp.database.dao.SyncDao;
import de.tum.in.tumcampusapp.database.dao.TransportDao;
import de.tum.in.tumcampusapp.database.dao.TumLockDao;
import de.tum.in.tumcampusapp.database.dao.WidgetsTimetableBlacklistDao;
import de.tum.in.tumcampusapp.database.dao.WifiMeasurementDao;
import de.tum.in.tumcampusapp.database.migrations.Migration1to2;
import de.tum.in.tumcampusapp.database.migrations.Migration2to3;
import de.tum.in.tumcampusapp.managers.CacheManager;
import de.tum.in.tumcampusapp.managers.CalendarManager;
import de.tum.in.tumcampusapp.models.cafeteria.Cafeteria;
import de.tum.in.tumcampusapp.models.cafeteria.CafeteriaMenu;
import de.tum.in.tumcampusapp.models.cafeteria.FavoriteDish;
import de.tum.in.tumcampusapp.models.cafeteria.Location;
import de.tum.in.tumcampusapp.models.dbEntities.ChatRoomDbRow;
import de.tum.in.tumcampusapp.models.dbEntities.OpenQuestions;
import de.tum.in.tumcampusapp.models.dbEntities.OwnQuestions;
import de.tum.in.tumcampusapp.models.dbEntities.Recent;
import de.tum.in.tumcampusapp.models.dbEntities.RoomLocations;
import de.tum.in.tumcampusapp.models.dbEntities.Sync;
import de.tum.in.tumcampusapp.models.dbEntities.TumLock;
import de.tum.in.tumcampusapp.models.dbEntities.WidgetsTimetableBlacklist;
import de.tum.in.tumcampusapp.models.gcm.GCMNotification;
import de.tum.in.tumcampusapp.models.transport.TransportFavorites;
import de.tum.in.tumcampusapp.models.transport.WidgetsTransport;
import de.tum.in.tumcampusapp.models.tumcabe.BuildingToGps;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
import de.tum.in.tumcampusapp.models.tumcabe.Faculty;
import de.tum.in.tumcampusapp.models.tumcabe.Kino;
import de.tum.in.tumcampusapp.models.tumcabe.News;
import de.tum.in.tumcampusapp.models.tumcabe.NewsSources;
import de.tum.in.tumcampusapp.models.tumcabe.StudyRoom;
import de.tum.in.tumcampusapp.models.tumcabe.StudyRoomGroup;
import de.tum.in.tumcampusapp.models.tumcabe.WifiMeasurement;
import de.tum.in.tumcampusapp.models.tumo.CalendarItem;
import de.tum.in.tumcampusapp.services.BackgroundService;
import de.tum.in.tumcampusapp.services.DownloadService;
import de.tum.in.tumcampusapp.services.SendMessageService;
import de.tum.in.tumcampusapp.services.SilenceService;

@Database(version = 3, entities = {
        Cafeteria.class,
        CafeteriaMenu.class,
        FavoriteDish.class,
        Sync.class,
        TumLock.class,
        BuildingToGps.class,
        Kino.class,
        ChatMessage.class,
        Location.class,
        News.class,
        NewsSources.class,
        CalendarItem.class,
        RoomLocations.class,
        WidgetsTimetableBlacklist.class,
        WifiMeasurement.class,
        Recent.class,
        Faculty.class,
        OpenQuestions.class,
        OwnQuestions.class,
        StudyRoomGroup.class,
        StudyRoom.class,
        GCMNotification.class,
        TransportFavorites.class,
        WidgetsTransport.class,
        ChatRoomDbRow.class
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

    public abstract NewsDao newsDao();

    public abstract NewsSourcesDao newsSourcesDao();

    public abstract CalendarDao calendarDao();

    public abstract RoomLocationsDao roomLocationsDao();

    public abstract WidgetsTimetableBlacklistDao widgetsTimetableBlacklistDao();

    public abstract WifiMeasurementDao wifiMeasurementDao();

    public abstract RecentsDao recentsDao();

    public abstract FacultyDao facultyDao();

    public abstract OpenQuestionsDao openQuestionsDao();

    public abstract OwnQuestionsDao ownQuestionsDao();

    public abstract StudyRoomGroupDao studyRoomGroupDao();

    public abstract StudyRoomDao studyRoomDao();

    public abstract NotificationDao notificationDao();

    public abstract TransportDao transportDao();

    public abstract ChatRoomDao chatRoomDao();

    private static TcaDb instance;

    public static synchronized TcaDb getInstance(Context context) {
        if (instance == null || !instance.isOpen()) {
            instance = Room.databaseBuilder(context.getApplicationContext(), TcaDb.class, Const.DATABASE_NAME)
                           .allowMainThreadQueries()
                           .addMigrations(new Migration1to2(), new Migration2to3())
                           .build();
        }
        return instance;
    }

    /**
     * Drop all tables, so we can do a complete clean start
     * Careful: After executing this method, almost all the managers are in an illegal state, and
     * can't do any SQL anymore. So take care to actually reinitialize all Managers
     *
     * @param c context
     */
    public static void resetDb(Context c) {
        // Stop all services, since they might have instantiated Managers and cause SQLExceptions
        Class<?>[] services = new Class<?>[]{
                CalendarManager.QueryLocationsService.class,
                SendMessageService.class,
                SilenceService.class,
                DownloadService.class,
                BackgroundService.class};
        for (Class<?> service : services) {
            c.stopService(new Intent(c, service));
        }

        //Clear our cache table
        CacheManager.clearCache(c);

        //Clear the db?
        //TODO remove this, as we want to keep the data
        TcaDb tdb = TcaDb.getInstance(c);
        tdb.cafeteriaDao()
           .removeCache();
        tdb.cafeteriaMenuDao()
           .removeCache();
        tdb.calendarDao()
           .flush();
        tdb.locationDao()
           .removeCache();
        tdb.newsDao()
           .flush();
        tdb.newsSourcesDao()
           .flush();
        tdb.recentsDao()
           .removeCache();
        tdb.roomLocationsDao()
           .flush();
        tdb.syncDao()
           .removeCache();
        tdb.chatMessageDao()
           .removeCache();
        tdb.chatRoomDao()
           .removeCache();
        tdb.openQuestionsDao()
           .flush();
        tdb.ownQuestionsDao()
           .flush();
        tdb.tumLockDao()
           .removeCache();
        tdb.facultyDao()
           .flush();
        tdb.transportDao()
           .removeCache();
        tdb.studyRoomDao()
           .removeCache();
        tdb.studyRoomGroupDao()
           .removeCache();
        tdb.kinoDao()
           .flush();
        tdb.widgetsTimetableBlacklistDao()
           .flush();
        tdb.notificationDao()
           .cleanup();
        tdb.favoriteDishDao()
           .removeCache();
        tdb.buildingToGpsDao()
           .removeCache();
        tdb.wifiMeasurementDao()
           .cleanup();
    }
}
