package de.tum.in.tumcampusapp.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import android.content.Context;
import android.content.Intent;

import de.tum.in.tumcampusapp.component.notifications.persistence.ActiveAlarm;
import de.tum.in.tumcampusapp.component.notifications.persistence.ActiveAlarmsDao;
import de.tum.in.tumcampusapp.component.notifications.persistence.ScheduledNotification;
import de.tum.in.tumcampusapp.component.notifications.persistence.ScheduledNotificationsDao;
import de.tum.in.tumcampusapp.component.other.general.NotificationDao;
import de.tum.in.tumcampusapp.component.other.general.RecentsDao;
import de.tum.in.tumcampusapp.component.other.general.model.Recent;
import de.tum.in.tumcampusapp.component.other.locations.BuildingToGpsDao;
import de.tum.in.tumcampusapp.component.other.locations.RoomLocationsDao;
import de.tum.in.tumcampusapp.component.other.locations.model.BuildingToGps;
import de.tum.in.tumcampusapp.component.other.wifimeasurement.WifiMeasurementDao;
import de.tum.in.tumcampusapp.component.other.wifimeasurement.model.WifiMeasurement;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarController;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarDao;
import de.tum.in.tumcampusapp.component.tumui.calendar.WidgetsTimetableBlacklistDao;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.WidgetsTimetableBlacklist;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.RoomLocations;
import de.tum.in.tumcampusapp.component.ui.alarm.model.FcmNotification;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaLocationDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.FavoriteDishDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.Cafeteria;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.FavoriteDish;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.Location;
import de.tum.in.tumcampusapp.component.ui.chat.ChatMessageDao;
import de.tum.in.tumcampusapp.component.ui.chat.ChatRoomDao;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoomDbRow;
import de.tum.in.tumcampusapp.component.ui.news.NewsDao;
import de.tum.in.tumcampusapp.component.ui.news.NewsSourcesDao;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.component.ui.studyroom.StudyRoomDao;
import de.tum.in.tumcampusapp.component.ui.studyroom.StudyRoomGroupDao;
import de.tum.in.tumcampusapp.component.ui.studyroom.model.StudyRoom;
import de.tum.in.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup;
import de.tum.in.tumcampusapp.component.ui.ticket.EventDao;
import de.tum.in.tumcampusapp.component.ui.ticket.TicketDao;
import de.tum.in.tumcampusapp.component.ui.ticket.TicketTypeDao;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.component.ui.transportation.TransportDao;
import de.tum.in.tumcampusapp.component.ui.transportation.model.TransportFavorites;
import de.tum.in.tumcampusapp.component.ui.transportation.model.WidgetsTransport;
import de.tum.in.tumcampusapp.component.ui.tufilm.KinoDao;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import de.tum.in.tumcampusapp.database.migrations.Migration1to2;
import de.tum.in.tumcampusapp.service.BackgroundService;
import de.tum.in.tumcampusapp.service.DownloadService;
import de.tum.in.tumcampusapp.service.SendMessageService;
import de.tum.in.tumcampusapp.service.SilenceService;
import de.tum.in.tumcampusapp.utils.CacheManager;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.sync.SyncDao;
import de.tum.in.tumcampusapp.utils.sync.model.Sync;

@Database(version = 2, entities = {
        Cafeteria.class,
        CafeteriaMenu.class,
        FavoriteDish.class,
        Sync.class,
        BuildingToGps.class,
        Kino.class,
        Event.class,
        Ticket.class,
        TicketType.class,
        ChatMessage.class,
        Location.class,
        News.class,
        NewsSources.class,
        CalendarItem.class,
        RoomLocations.class,
        WidgetsTimetableBlacklist.class,
        WifiMeasurement.class,
        Recent.class,
        StudyRoomGroup.class,
        StudyRoom.class,
        FcmNotification.class,
        TransportFavorites.class,
        WidgetsTransport.class,
        ChatRoomDbRow.class,
        ScheduledNotification.class,
        ActiveAlarm.class
})
@TypeConverters(Converters.class)
public abstract class TcaDb extends RoomDatabase {
    private static final Migration[] migrations = {
            new Migration1to2()
    };

    private static TcaDb instance;

    public static synchronized TcaDb getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), TcaDb.class, Const.DATABASE_NAME)
                    .allowMainThreadQueries()
                    .addMigrations(migrations)
                    .build();
        }
        return instance;
    }

    public abstract CafeteriaDao cafeteriaDao();

    public abstract CafeteriaMenuDao cafeteriaMenuDao();

    public abstract FavoriteDishDao favoriteDishDao();

    public abstract SyncDao syncDao();

    public abstract BuildingToGpsDao buildingToGpsDao();

    public abstract KinoDao kinoDao();

    public abstract EventDao eventDao();

    public abstract TicketDao ticketDao();

    public abstract TicketTypeDao ticketTypeDao();

    public abstract CafeteriaLocationDao locationDao();

    public abstract ChatMessageDao chatMessageDao();

    public abstract NewsDao newsDao();

    public abstract NewsSourcesDao newsSourcesDao();

    public abstract CalendarDao calendarDao();

    public abstract RoomLocationsDao roomLocationsDao();

    public abstract WidgetsTimetableBlacklistDao widgetsTimetableBlacklistDao();

    public abstract WifiMeasurementDao wifiMeasurementDao();

    public abstract RecentsDao recentsDao();

    public abstract StudyRoomGroupDao studyRoomGroupDao();

    public abstract StudyRoomDao studyRoomDao();

    public abstract NotificationDao notificationDao();

    public abstract TransportDao transportDao();

    public abstract ChatRoomDao chatRoomDao();

    public abstract ScheduledNotificationsDao scheduledNotificationsDao();

    public abstract ActiveAlarmsDao activeNotificationsDao();

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
                CalendarController.QueryLocationsService.class,
                SendMessageService.class,
                SilenceService.class,
                DownloadService.class,
                BackgroundService.class};
        for (Class<?> service : services) {
            c.stopService(new Intent(c, service));
        }

        // Clear our cache table
        CacheManager cacheManager = new CacheManager(c);
        cacheManager.clearCache();

        TcaDb.getInstance(c).clearAllTables();
    }
}
