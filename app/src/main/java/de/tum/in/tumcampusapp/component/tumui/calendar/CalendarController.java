package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.CalendarContract;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.notifications.NotificationScheduler;
import de.tum.in.tumcampusapp.component.notifications.ProvidesNotifications;
import de.tum.in.tumcampusapp.component.notifications.model.FutureNotification;
import de.tum.in.tumcampusapp.component.other.locations.RoomLocationsDao;
import de.tum.in.tumcampusapp.component.other.locations.model.Geo;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.Event;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.WidgetsTimetableBlacklist;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.RoomLocation;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import de.tum.in.tumcampusapp.utils.sync.SyncManager;

import static android.Manifest.permission.WRITE_CALENDAR;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Calendar Manager, handles database stuff, external imports.
 */
public class CalendarController implements ProvidesNotifications {

    // TODO: Create CalendarLocalRepo & CalendarRemoteRepo
    private static final String[] PROJECTION = {"_id", "name"};

    private final Context mContext;

    private final CalendarDao calendarDao;
    private final RoomLocationsDao roomLocationsDao;
    private final WidgetsTimetableBlacklistDao widgetsTimetableBlacklistDao;
    private final NotificationScheduler notificationScheduler;

    @Inject
    public CalendarController(Context context, NotificationScheduler scheduler) {
        mContext = context;
        calendarDao = TcaDb.getInstance(context).calendarDao(); // TODO: Inject DB
        roomLocationsDao = TcaDb.getInstance(context).roomLocationsDao();
        widgetsTimetableBlacklistDao = TcaDb.getInstance(context).widgetsTimetableBlacklistDao();
        notificationScheduler = scheduler;
    }

    /**
     * Replaces the current TUM_CAMPUS_APP calendar with a new version.
     */
    public void syncCalendar() throws SQLiteException {
        // Deleting earlier calendar created by TUM Campus App
        deleteLocalCalendar();
        Uri uri = CalendarHelper.addCalendar(mContext);
        addEvents(uri);
    }

    /**
     * Deletes a local Google calendar
     *
     * @return Number of rows deleted
     */
    public int deleteLocalCalendar() {
        return CalendarHelper.deleteCalendar(mContext);
    }

    /**
     * Adds events to the content provider
     */
    private void addEvents(Uri uri) throws SQLiteException {
        if (ContextCompat.checkSelfPermission(mContext, WRITE_CALENDAR) != PERMISSION_GRANTED) {
            return;
        }

        // Get ID
        ContentResolver contentResolver = mContext.getContentResolver();
        String id = "0";
        try (Cursor cursor = contentResolver.query(uri, PROJECTION, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                id = cursor.getString(0);
            }
        }

        CalendarDao calendarDao = TcaDb.getInstance(mContext).calendarDao();
        List<CalendarItem> calendarItems = calendarDao.getAllNotCancelled();

        for (CalendarItem calendarItem : calendarItems) {
            ContentValues values = calendarItem.toContentValues();
            values.put(CalendarContract.Events.CALENDAR_ID, id);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, mContext.getString(R.string.calendarTimeZone));
            contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);
        }
    }

    List<CalendarItem> getFromDbBetweenDates(DateTime begin, DateTime end) {
        return calendarDao.getAllBetweenDates(begin, end);
    }

    List<CalendarItem> getFromDbNotCancelledBetweenDates(DateTime begin, DateTime end) {
        return calendarDao.getAllNotCancelledBetweenDates(begin, end);
    }

    /**
     * Returns all stored events in the next days from db.
     * If there is a valid widget id (> 0) the events are filtered by the widgets blacklist
     *
     * @param dayCount The number of days
     * @param widgetId The id of the widget
     * @return List<IntegratedCalendarEvent> List of Events
     */
    public List<IntegratedCalendarEvent> getNextDaysFromDb(int dayCount, int widgetId) {
        DateTime fromDate = DateTime.now();
        DateTime toDate = fromDate.plusDays(dayCount);

        List<IntegratedCalendarEvent> calendarEvents = new ArrayList<>();
        List<CalendarItem> calendarItems = calendarDao.getNextDays(fromDate, toDate, String.valueOf(widgetId));
        for (CalendarItem calendarItem : calendarItems) {
            calendarEvents.add(new IntegratedCalendarEvent(calendarItem, mContext));
        }

        return calendarEvents;
    }

    /**
     * Get current lecture from the database
     */
    public List<CalendarItem> getCurrentFromDb() {
        return calendarDao.getCurrentLectures();
    }

    /**
     * Checks if there are any event in the database
     *
     * @return True if there are lectures in the database, false if there is no lecture
     */
    public boolean hasLectures() {
        return calendarDao.hasLectures();
    }

    /**
     * Add a lecture to the blacklist of a widget
     *
     * @param widgetId the Id of the widget
     * @param lecture  the title of the lecture
     */
    public void addLectureToBlacklist(int widgetId, String lecture) {
        widgetsTimetableBlacklistDao.insert(new WidgetsTimetableBlacklist(widgetId, lecture));
    }

    /**
     * Remove a lecture from the blacklist of a widget
     *
     * @param widgetId the Id of the widget
     * @param lecture  the title of the lecture
     */
    public void deleteLectureFromBlacklist(int widgetId, String lecture) {
        widgetsTimetableBlacklistDao.delete(new WidgetsTimetableBlacklist(widgetId, lecture));
    }

    /**
     * get all lectures and the information whether they are on the blacklist for the given widget
     *
     * @param widgetId the Id of the widget
     * @return A cursor containing a list of lectures and the is_on_blacklist flag
     */
    public List<CalendarItem> getLecturesForWidget(int widgetId) {
        List<CalendarItem> lectures = calendarDao.getLecturesInBlacklist(Integer.toString(widgetId));
        for (CalendarItem blacklistedLecture : lectures) {
            blacklistedLecture.setBlacklisted(true);
        }
        lectures.addAll(calendarDao.getLecturesNotInBlacklist(Integer.toString(widgetId)));
        return lectures;
    }

    /**
     * Gets the event by its id and duplicates of this event with different locations.
     * The first item is the one with the given id.
     */
    @Nullable
    List<CalendarItem> getCalendarItemAndDuplicatesById(String id) {
        return calendarDao.getCalendarItemsById(id);
    }

    void scheduleNotifications(List<Event> events) {
        // Be responsible when scheduling alarms. We don't want to exceed system resources
        // By only using up half of the remaining resources, we achieve fair distribution of the
        // remaining usable notifications
        int maxNotificationsToSchedule = NotificationScheduler.maxRemainingAlarms(mContext) / 2;

        List<FutureNotification> notifications = new ArrayList<>();
        for (Event event : events) {
            if (event.isFutureEvent()) {
                FutureNotification notification = event.toNotification(mContext);
                if (notification != null) {
                    notifications.add(notification);
                    if (notifications.size() >= maxNotificationsToSchedule) {
                        break;
                    }
                }
            }
        }

        notificationScheduler.schedule(notifications);
    }

    public void importCalendar(@NonNull List<Event> events) {
        // Cleanup cache before importing
        removeCache();

        // Import the new events
        try {
            replaceIntoDb(events);
        } catch (Exception e) {
            Utils.log(e);
        }

        new SyncManager(mContext).replaceIntoDb(Const.SYNC_CALENDAR_IMPORT);
    }

    /**
     * Removes all cache items
     */
    private void removeCache() {
        calendarDao.flush();
    }

    private void replaceIntoDb(List<Event> events) {
        List<CalendarItem> items = new ArrayList<>();
        for (Event event : events) {
            if (event.getId() == null || event.getId().isEmpty()) {
                throw new IllegalArgumentException("Invalid id.");
            }

            if (event.getTitle().isEmpty()) {
                throw new IllegalArgumentException("Invalid lecture title.");
            }

            items.add(event.toCalendarItem());
        }

        calendarDao.insert(items.toArray(new CalendarItem[0]));
    }

    /**
     * Gets the next lectures that could be important to the user
     */
    public List<CalendarItem> getNextCalendarItems() {
        return calendarDao.getNextCalendarItems();
    }

    @Nullable
    List<String> getLocationsForEvent(String eventId) {
        return calendarDao.getNonCancelledLocationsById(eventId);
    }

    /**
     * Gets the coordinates of the next lecture or the current running lecture,
     * if it started during the last 30 minutes
     */
    public Geo getNextCalendarItemGeo() {
        Geo geo = null;
        RoomLocation roomLocation = roomLocationsDao.getNextLectureCoordinates();
        if (roomLocation != null) {
            geo = roomLocation.toGeo();
        }
        return geo;
    }

    @Override
    public boolean hasNotificationsEnabled() {
        return Utils.getSettingBool(mContext, "card_next_phone", false);
    }

}
