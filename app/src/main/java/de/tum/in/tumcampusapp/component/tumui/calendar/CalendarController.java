package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.common.base.Optional;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.other.locations.LocationManager;
import de.tum.in.tumcampusapp.component.other.locations.RoomLocationsDao;
import de.tum.in.tumcampusapp.component.other.locations.model.Geo;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.Event;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.Events;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.WidgetsTimetableBlacklist;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.RoomLocations;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import de.tum.in.tumcampusapp.utils.sync.SyncManager;

/**
 * Calendar Manager, handles database stuff, external imports.
 */
public class CalendarController implements ProvidesCard {
    private static final String[] PROJECTION = {"_id", "name"};

    private static final int TIME_TO_SYNC_CALENDAR = 604800; // 1 week

    private final CalendarDao calendarDao;

    private final RoomLocationsDao roomLocationsDao;

    private final WidgetsTimetableBlacklistDao widgetsTimetableBlacklistDao;
    private final Context mContext;

    public CalendarController(Context context) {
        mContext = context;
        calendarDao = TcaDb.getInstance(context)
                .calendarDao();
        roomLocationsDao = TcaDb.getInstance(context)
                .roomLocationsDao();
        widgetsTimetableBlacklistDao = TcaDb.getInstance(context)
                .widgetsTimetableBlacklistDao();
    }

    /**
     * Replaces the current TUM_CAMPUS_APP calendar with a new version.
     *
     * @param c Context
     */
    public static void syncCalendar(Context c) throws SQLiteException {
        // Deleting earlier calendar created by TUM Campus App
        deleteLocalCalendar(c);
        Uri uri = CalendarHelper.addCalendar(c);
        addEvents(c, uri);
    }

    /**
     * Deletes a local Google calendar
     *
     * @return Number of rows deleted
     */
    public static int deleteLocalCalendar(Context c) {
        return CalendarHelper.deleteCalendar(c);
    }

    private static void addEvents(Context c, Uri uri) throws SQLiteException {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Get ID
        ContentResolver contentResolver = c.getContentResolver();
        String id = "0";
        try (Cursor cursor = contentResolver.query(uri, PROJECTION, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                id = cursor.getString(0);
            }
        }

        CalendarDao calendarDao = TcaDb.getInstance(c).calendarDao();
        List<CalendarItem> calendarItems = calendarDao.getAllNotCancelled();

        for (CalendarItem calendarItem : calendarItems) {
            ContentValues values = calendarItem.toContentValues();
            values.put(CalendarContract.Events.CALENDAR_ID, id);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, c.getString(R.string.calendarTimeZone));
            contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);
        }
    }

    public List<CalendarItem> getFromDbBetweenDates(DateTime begin, DateTime end) {
        return calendarDao.getAllBetweenDates(begin, end);
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

    @Nullable
    public CalendarItem getCalendarItemById(String id) {
        return calendarDao.getCalendarItemById(id);
    }

    public void importCalendar(Events newEvents) {
        // Cleanup cache before importing
        removeCache();

        // Import the new events
        List<Event> events = newEvents.getEvents();
        if (events != null) {
            try {
                replaceIntoDb(events);
            } catch (Exception e) {
                Utils.log(e);
            }
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

        calendarDao.insert(items.toArray(new CalendarItem[items.size()]));
    }

    /**
     * Gets the next lectures that could be important to the user
     */
    public List<CalendarItem> getNextCalendarItems() {
        return calendarDao.getNextCalendarItems();
    }

    /**
     * Gets the coordinates of the next lecture or the current running lecture,
     * if it started during the last 30 minutes
     */
    public Geo getNextCalendarItemGeo() {
        Geo geo = null;
        RoomLocations roomLocation = roomLocationsDao.getNextLectureCoordinates();
        if (roomLocation != null) {
            geo = roomLocation.toGeo();
        }
        return geo;
    }

    @NotNull
    @Override
    public List<Card> getCards(@NonNull CacheControl cacheControl) {
        List<CalendarItem> nextCalendarItems = getNextCalendarItems();
        List<Card> results = new ArrayList<>();

        if (!nextCalendarItems.isEmpty()) {
            NextLectureCard card = new NextLectureCard(mContext);
            card.setLectures(nextCalendarItems);
            results.add(card.getIfShowOnStart());
        }

        return results;
    }

    public static class QueryLocationsService extends IntentService {

        private static final String QUERY_LOCATIONS = "query_locations";

        public QueryLocationsService() {
            super(QUERY_LOCATIONS);
        }

        public static void loadGeo(Context c) {
            LocationManager locationManager = new LocationManager(c);
            final CalendarDao calendarDao = TcaDb.getInstance(c)
                    .calendarDao();
            final RoomLocationsDao roomLocationsDao = TcaDb.getInstance(c)
                    .roomLocationsDao();

            List<CalendarItem> calendarItems = calendarDao.getLecturesWithoutCoordinates();
            for (CalendarItem calendarItem : calendarItems) {
                String location = calendarItem.getLocation();
                if (location.isEmpty()) {
                    continue;
                }

                Optional<Geo> geo = locationManager.roomLocationStringToGeo(location);
                if (geo.isPresent()) {
                    Utils.logv("inserted " + location + ' ' + geo);
                    roomLocationsDao.insert(new RoomLocations(location, geo.get()));
                }
            }

            // Do sync of google calendar if necessary
            boolean syncCalendar = Utils.getSettingBool(c, Const.SYNC_CALENDAR, false)
                    && ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_CALENDAR) ==
                    PackageManager.PERMISSION_GRANTED;

            SyncManager syncManager = new SyncManager(c);
            if (!syncCalendar || !syncManager.needSync(Const.SYNC_CALENDAR, TIME_TO_SYNC_CALENDAR)) {
                return;
            }

            try {
                syncCalendar(c);
                syncManager.replaceIntoDb(Const.SYNC_CALENDAR);
            } catch (SQLiteException e) {
                Utils.log(e);
            }
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            new Thread(() -> loadGeo(QueryLocationsService.this)).start();
        }
    }
}
