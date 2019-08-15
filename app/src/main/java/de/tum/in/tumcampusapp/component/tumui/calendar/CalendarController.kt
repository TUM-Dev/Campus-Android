package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.notifications.ProvidesNotifications
import de.tum.`in`.tumcampusapp.component.other.locations.RoomLocationsDao
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.Event
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.WidgetsTimetableBlacklist
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.ProvidesCard
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.sync.SyncManager
import org.joda.time.DateTime
import java.util.*

/**
 * Calendar Manager, handles database stuff, external imports.
 */
class CalendarController(private val context: Context) : ProvidesCard, ProvidesNotifications {

    private val calendarDao: CalendarDao = TcaDb.getInstance(context).calendarDao()
    private val roomLocationsDao: RoomLocationsDao = TcaDb.getInstance(context).roomLocationsDao()
    private val widgetsTimetableBlacklistDao: WidgetsTimetableBlacklistDao = TcaDb.getInstance(context).widgetsTimetableBlacklistDao()

    /**
     * Get current lecture from the database
     */
    val currentLectures: List<CalendarItem>
        get() = calendarDao.currentLectures

    /**
     * Gets the next lectures that could be important to the user
     */
    val nextCalendarItems: List<CalendarItem>
        get() = calendarDao.nextCalendarItems

    /**
     * Gets the coordinates of the next lecture or the current running lecture,
     * if it started during the last 30 minutes
     */
    val nextCalendarItemGeo: Geo?
        get() = roomLocationsDao.nextLectureCoordinates?.toGeo()

    fun getFromDbBetweenDates(begin: DateTime, end: DateTime) =
            applyEventColors(calendarDao.getAllBetweenDates(begin, end))


    fun getFromDbNotCancelledBetweenDates(begin: DateTime, end: DateTime) =
            applyEventColors(calendarDao.getAllNotCancelledBetweenDates(begin, end))


    private fun applyEventColors(calendarItems: List<CalendarItem>): List<CalendarItem> {
        val provider = EventColorProvider(context)
        calendarItems.forEach {
            it.color = provider.getColor(it)
        }
        return calendarItems
    }

    /**
     * Returns all stored events in the next days from db.
     * If there is a valid widget id (> 0) the events are filtered by the widgets blacklist
     *
     * @param dayCount The number of days
     * @param widgetId The id of the widget
     * @return List<IntegratedCalendarEvent> List of Events
    </IntegratedCalendarEvent> */
    fun getNextDaysFromDb(dayCount: Int, widgetId: Int): List<WidgetCalendarItem> {
        val fromDate = DateTime.now()
        val toDate = fromDate.plusDays(dayCount)

        val provider = EventColorProvider(context)
        // query already filters out blacklisted events
        val calendarItems = calendarDao.getNextDays(fromDate, toDate, widgetId.toString())

        return calendarItems.map {
            WidgetCalendarItem.create(it).apply { color = provider.getColor(it) }
        }
    }

    /**
     * Checks if there are any event in the database
     *
     * @return True if there are lectures in the database, false if there is no lecture
     */
    fun hasLectures() = calendarDao.hasLectures()

    /**
     * Add a lecture to the blacklist of a widget
     *
     * @param widgetId the Id of the widget
     * @param lecture  the title of the lecture
     */
    fun addLectureToBlacklist(widgetId: Int, lecture: String) {
        widgetsTimetableBlacklistDao.insert(WidgetsTimetableBlacklist(widgetId, lecture))
    }

    /**
     * Remove a lecture from the blacklist of a widget
     *
     * @param widgetId the Id of the widget
     * @param lecture  the title of the lecture
     */
    fun deleteLectureFromBlacklist(widgetId: Int, lecture: String) {
        widgetsTimetableBlacklistDao.delete(WidgetsTimetableBlacklist(widgetId, lecture))
    }

    /**
     * Get all lectures and the information whether they are on the blacklist for the given widget
     *
     * @param widgetId the Id of the widget
     * @return A cursor containing a list of lectures and the is_on_blacklist flag
     */
    fun getLecturesForWidget(widgetId: Int): List<CalendarItem> {
        val lectures = calendarDao.getLecturesInBlacklist(widgetId.toString()).toMutableList()
        lectures.forEach {
            it.blacklisted = true
        }
        lectures.addAll(calendarDao.getLecturesNotInBlacklist(widgetId.toString()))
        return lectures
    }

    /**
     * Gets the event by its id and duplicates of this event with different locations.
     * The first item is the one with the given id.
     */
    fun getCalendarItemAndDuplicatesById(id: String) = calendarDao.getCalendarItemsById(id)

    fun scheduleNotifications(events: List<Event>) {
        // Be responsible when scheduling alarms. We don't want to exceed system resources
        // By only using up half of the remaining resources, we achieve fair distribution of the
        // remaining usable notifications
        val maxNotificationsToSchedule = NotificationScheduler.maxRemainingAlarms(context) / 2

        val notifications = events.filter { it.isFutureEvent }
                .mapNotNull { it.toNotification(context) }
                .take(maxNotificationsToSchedule)

        val scheduler = NotificationScheduler(context)
        scheduler.schedule(notifications)
    }

    fun importCalendar(events: List<Event>) {
        // Cleanup cache before importing
        removeCache()

        // Import the new events
        try {
            replaceIntoDb(events)
        } catch (e: Exception) {
            Utils.log(e)
        }
        SyncManager(context).replaceIntoDb(Const.SYNC_CALENDAR_IMPORT)
    }

    /**
     * Removes all cache items
     */
    private fun removeCache() {
        calendarDao.flush()
    }

    private fun replaceIntoDb(events: List<Event>) {
        val items = ArrayList<CalendarItem>()
        for (event in events) {
            if (event.id != null && event.id.isNotEmpty() && event.title.isNotEmpty()) {
                items.add(event.toCalendarItem())
            }
        }
        calendarDao.insert(*items.toTypedArray())
    }

    fun getLocationsForEvent(eventId: String): List<String> {
        return calendarDao.getNonCancelledLocationsById(eventId)
    }

    override fun getCards(cacheControl: CacheControl): List<Card> {
        val nextCalendarItems = calendarDao.nextUniqueCalendarItems
        val results = ArrayList<Card>()

        if (nextCalendarItems.isNotEmpty()) {
            val card = NextLectureCard(context)
            card.setLectures(nextCalendarItems)

            card.getIfShowOnStart()?.let {
                results.add(it)
            }
        }
        return results
    }

    override fun hasNotificationsEnabled() = Utils.getSettingBool(context, "card_next_phone", false)

    companion object {

        private val PROJECTION = arrayOf("_id", "name")

        /**
         * Replaces the current TUM_CAMPUS_APP calendar with a new version.
         *
         * @param context Context
         */
        @Throws(SQLiteException::class)
        @JvmStatic
        fun syncCalendar(context: Context) {
            // Deleting earlier calendar created by TUM Campus App
            deleteLocalCalendar(context)
            CalendarHelper.addCalendar(context)?.let {
                addEvents(context, it)
            }
        }

        /**
         * Deletes a local Google calendar
         *
         * @return Number of rows deleted
         */
        fun deleteLocalCalendar(c: Context) = CalendarHelper.deleteCalendar(c)

        /**
         * Adds events to the content provider
         */
        @Throws(SQLiteException::class)
        private fun addEvents(context: Context, uri: Uri) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            // Get ID
            val contentResolver = context.contentResolver
            var id = "0"
            contentResolver.query(uri, PROJECTION, null, null, null).use { cursor ->
                while (cursor?.moveToNext() == true) {
                    id = cursor.getString(0)
                }
            }

            val calendarDao = TcaDb.getInstance(context).calendarDao()
            val calendarItems = calendarDao.allNotCancelled

            for (calendarItem in calendarItems) {
                val values = calendarItem.toContentValues().apply {
                    put(CalendarContract.Events.CALENDAR_ID, id)
                    put(CalendarContract.Events.EVENT_TIMEZONE, context.getString(R.string.calendarTimeZone))
                }
                contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            }
        }
    }

}
