package de.tum.`in`.tumcampusapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.work.WorkManager
import de.tum.`in`.tumcampusapp.component.notifications.persistence.ActiveAlarm
import de.tum.`in`.tumcampusapp.component.notifications.persistence.ActiveAlarmsDao
import de.tum.`in`.tumcampusapp.component.notifications.persistence.ScheduledNotification
import de.tum.`in`.tumcampusapp.component.notifications.persistence.ScheduledNotificationsDao
import de.tum.`in`.tumcampusapp.component.other.general.NotificationDao
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.other.locations.BuildingToGpsDao
import de.tum.`in`.tumcampusapp.component.other.locations.RoomLocationsDao
import de.tum.`in`.tumcampusapp.component.other.locations.model.BuildingToGps
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarDao
import de.tum.`in`.tumcampusapp.component.tumui.calendar.WidgetsTimetableBlacklistDao
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.WidgetsTimetableBlacklist
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.RoomLocations
import de.tum.`in`.tumcampusapp.component.ui.alarm.model.FcmNotification
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaDao
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaMenuDao
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.FavoriteDishDao
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.FavoriteDish
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatMessageDao
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatRoomDao
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoomDbRow
import de.tum.`in`.tumcampusapp.component.ui.news.NewsDao
import de.tum.`in`.tumcampusapp.component.ui.news.NewsSourcesDao
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsSources
import de.tum.`in`.tumcampusapp.component.ui.openinghour.LocationDao
import de.tum.`in`.tumcampusapp.component.ui.openinghour.model.Location
import de.tum.`in`.tumcampusapp.component.ui.studyroom.StudyRoomDao
import de.tum.`in`.tumcampusapp.component.ui.studyroom.StudyRoomGroupDao
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoom
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventDao
import de.tum.`in`.tumcampusapp.component.ui.ticket.TicketDao
import de.tum.`in`.tumcampusapp.component.ui.ticket.TicketTypeDao
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketType
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportDao
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.TransportFavorites
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.WidgetsTransport
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoDao
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.database.migrations.Migration1to2
import de.tum.`in`.tumcampusapp.database.migrations.Migration2to3
import de.tum.`in`.tumcampusapp.database.migrations.Migration3to4
import de.tum.`in`.tumcampusapp.database.migrations.Migration4to5
import de.tum.`in`.tumcampusapp.utils.CacheManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.sync.SyncDao
import de.tum.`in`.tumcampusapp.utils.sync.model.Sync
import java.util.concurrent.ExecutionException

@Database(version = 5, entities = [
    Cafeteria::class,
    CafeteriaMenu::class,
    FavoriteDish::class,
    Sync::class,
    BuildingToGps::class,
    Kino::class,
    Event::class,
    Ticket::class,
    TicketType::class,
    ChatMessage::class,
    Location::class,
    News::class,
    NewsSources::class,
    CalendarItem::class,
    RoomLocations::class,
    WidgetsTimetableBlacklist::class,
    Recent::class,
    StudyRoomGroup::class,
    StudyRoom::class,
    FcmNotification::class,
    TransportFavorites::class,
    WidgetsTransport::class,
    ChatRoomDbRow::class,
    ScheduledNotification::class,
    ActiveAlarm::class])
@TypeConverters(Converters::class)
abstract class TcaDb : RoomDatabase() {

    abstract fun cafeteriaDao(): CafeteriaDao

    abstract fun cafeteriaMenuDao(): CafeteriaMenuDao

    abstract fun favoriteDishDao(): FavoriteDishDao

    abstract fun syncDao(): SyncDao

    abstract fun buildingToGpsDao(): BuildingToGpsDao

    abstract fun kinoDao(): KinoDao

    abstract fun eventDao(): EventDao

    abstract fun ticketDao(): TicketDao

    abstract fun ticketTypeDao(): TicketTypeDao

    abstract fun locationDao(): LocationDao

    abstract fun chatMessageDao(): ChatMessageDao

    abstract fun newsDao(): NewsDao

    abstract fun newsSourcesDao(): NewsSourcesDao

    abstract fun calendarDao(): CalendarDao

    abstract fun roomLocationsDao(): RoomLocationsDao

    abstract fun widgetsTimetableBlacklistDao(): WidgetsTimetableBlacklistDao

    abstract fun recentsDao(): RecentsDao

    abstract fun studyRoomGroupDao(): StudyRoomGroupDao

    abstract fun studyRoomDao(): StudyRoomDao

    abstract fun notificationDao(): NotificationDao

    abstract fun transportDao(): TransportDao

    abstract fun chatRoomDao(): ChatRoomDao

    abstract fun scheduledNotificationsDao(): ScheduledNotificationsDao

    abstract fun activeNotificationsDao(): ActiveAlarmsDao

    companion object {
        private val migrations = arrayOf(
                Migration1to2(),
                Migration2to3(),
                Migration3to4(),
                Migration4to5()
        )

        private var instance: TcaDb? = null

        @Synchronized
        fun getInstance(context: Context): TcaDb {
            var instance = this.instance
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext, TcaDb::class.java, Const.DATABASE_NAME)
                        .allowMainThreadQueries()
                        .addMigrations(*migrations)
                        .build()
                this.instance = instance
            }
            return instance
        }

        /**
         * Drop all tables, so we can do a complete clean start
         * Careful: After executing this method, almost all the managers are in an illegal state, and
         * can't do any SQL anymore. So take care to actually reinitialize all Managers
         *
         * @param c context
         */
        @Throws(ExecutionException::class, InterruptedException::class)
        fun resetDb(c: Context) {
            // Stop all work tasks in WorkManager, since they might access the DB
            WorkManager.getInstance().cancelAllWork().result.get()

            // Clear our cache table
            val cacheManager = CacheManager(c)
            cacheManager.clearCache()

            TcaDb.getInstance(c).clearAllTables()
        }
    }
}
