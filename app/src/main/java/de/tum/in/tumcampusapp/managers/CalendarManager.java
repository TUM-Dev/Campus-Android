package de.tum.in.tumcampusapp.managers;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.auxiliary.calendar.CalendarHelper;
import de.tum.in.tumcampusapp.auxiliary.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampusapp.cards.NextLectureCard;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dao.CalendarDao;
import de.tum.in.tumcampusapp.database.dao.RoomLocationsDao;
import de.tum.in.tumcampusapp.database.dao.WidgetsTimetableBlacklistDao;
import de.tum.in.tumcampusapp.models.dbEntities.RoomLocations;
import de.tum.in.tumcampusapp.models.tumo.CalendarItem;
import de.tum.in.tumcampusapp.models.tumo.CalendarRow;
import de.tum.in.tumcampusapp.models.tumo.CalendarRowSet;
import de.tum.in.tumcampusapp.models.tumo.Geo;

/**
 * Calendar Manager, handles database stuff, external imports
 */
public class CalendarManager extends AbstractManager implements Card.ProvidesCard {
    private static final String[] PROJECTION = {"_id", "name"};

    private static final int TIME_TO_SYNC_CALENDAR = 604800; // 1 week

    private final CalendarDao calendarDao;

    private final RoomLocationsDao roomLocationsDao;

    private final WidgetsTimetableBlacklistDao widgetsTimetableBlacklistDao;

    public CalendarManager(Context context) {
        super(context);
        calendarDao = TcaDb.getInstance(context).calendarDao();
        roomLocationsDao = TcaDb.getInstance(context).roomLocationsDao();
        widgetsTimetableBlacklistDao = TcaDb.getInstance(context).widgetsTimetableBlacklistDao();
    }

    /**
     * Replaces the current TUM_CAMPUS_APP calendar with a new version
     *
     * @param c Context
     */
    public static void syncCalendar(Context c) {
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

    private static void addEvents(Context c, Uri uri) {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Get ID
        ContentResolver contentResolver = c.getContentResolver();
        String id = "0";
        try (Cursor cursor = contentResolver.query(uri, PROJECTION, null, null, null)) {
            while (cursor.moveToNext()) {
                id = cursor.getString(0);
            }
        }

        CalendarDao calendarDao = TcaDb.getInstance(c).calendarDao();

        List<CalendarItem> calendarItems = calendarDao.getAllNotCancelled();
        for (CalendarItem calendarItem: calendarItems) {
            final String title = calendarItem.getTitle();
            final String description = calendarItem.getDescription();
            final String strStart = calendarItem.getDtstart();
            final String strEnd = calendarItem.getDtend();
            final String location = calendarItem.getLocation();

            // Get start and end time
            long startMillis = Utils.getDateTime(strStart).getTime();
            long endMillis = Utils.getDateTime(strEnd).getTime();

            ContentValues values = new ContentValues();

            // Put the received values into a contentResolver to
            // transmit the to Google Calendar
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, title);
            values.put(CalendarContract.Events.DESCRIPTION, description);
            values.put(CalendarContract.Events.CALENDAR_ID, id);
            values.put(CalendarContract.Events.EVENT_LOCATION, location);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, R.string.calendarTimeZone);
            contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);
        }
    }

    public List<CalendarItem> getFromDbForDate(Date date) {
        // Format the requested date
        String requestedDateString = Utils.getDateString(date);

        // Fetch the data
        return calendarDao.getAllByDateNotCancelled("%" + requestedDateString + "%");
    }

    /**
     * Returns all stored events in the next days from db
     * If there is a valid widget id (> 0) the events are filtered by the widgets blacklist
     *
     * @param dayCount The number of days
     * @param widgetId The id of the widget
     * @return List<IntegratedCalendarEvent> List of Events
     */
    public List<IntegratedCalendarEvent> getNextDaysFromDb(int dayCount, int widgetId) {
        Calendar calendar = Calendar.getInstance();
        String from = Utils.getDateTimeString(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, dayCount);
        String to = Utils.getDateTimeString(calendar.getTime());

        List<IntegratedCalendarEvent> calendarEvents = new ArrayList<>();
        List<CalendarItem> calendarItems = calendarDao.getNextDays(from, to, String.valueOf(widgetId));
        for (CalendarItem calendarItem: calendarItems) {
            calendarEvents.add(new IntegratedCalendarEvent(calendarItem));
        }

        return calendarEvents;
    }

    /**
     * Get current lecture from the database
     *
     * @return
     */
    public Cursor getCurrentFromDb() {
        return calendarDao.getCurrentLecture();
    }

    /**
     * Checks if there are any event in the database
     *
     * @return True if there are lectures in the database, false if there is no lecture
     */
    public boolean hasLectures() {
        boolean result = false;
        try (Cursor c = calendarDao.hasLectures()) {
            if (c.moveToNext()) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Add a lecture to the blacklist of a widget
     *
     * @param widgetId the Id of the widget
     * @param lecture  the title of the lecture
     */
    public void addLectureToBlacklist(int widgetId, String lecture) {
        ContentValues values = new ContentValues();
        values.put("widget_id", widgetId);
        values.put("lecture_title", lecture);
        db.replace("widgets_timetable_blacklist", null, values);
    }

    /**
     * Remove a lecture from the blacklist of a widget
     *
     * @param widgetId the Id of the widget
     * @param lecture  the title of the lecture
     */
    public void deleteLectureFromBlacklist(int widgetId, String lecture) {
        db.delete("widgets_timetable_blacklist", "widget_id = ? AND lecture_title = ?",
                  new String[]{String.valueOf(widgetId), lecture});
    }

    /**
     * get all lectures and the information whether they are on the blacklist for the given widget
     *
     * @param widgetId the Id of the widget
     * @return A cursor containing a list of lectures and the is_on_blacklist flag
     */
    public Cursor getLecturesFromWidget(int widgetId) {
        return calendarDao.getBlacklistedLectures(String.valueOf(widgetId));
    }

    public void importCalendar(CalendarRowSet myCalendarList) {

        // Cleanup cache before importing
        removeCache();

        // reading xml
        List<CalendarRow> myCalendar = myCalendarList.getKalendarList();
        if (myCalendar != null) {
            for (CalendarRow row : myCalendar) {
                // insert into database
                try {
                    replaceIntoDb(row);
                } catch (Exception e) {
                    Utils.log(e);
                }
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

    void replaceIntoDb(CalendarRow row) {
        if (row.getNr()
               .isEmpty()) {
            throw new IllegalArgumentException("Invalid id.");
        }

        if (row.getTitle()
               .isEmpty()) {
            throw new IllegalArgumentException("Invalid lecture Title.");
        }

        calendarDao.insert(new CalendarItem(row.getNr(), row.getStatus(), row.getUrl(),
                                            row.getTitle(), row.getDescription(),
                                            row.getDtstart(), row.getDtend(), row.getLocation()));
    }

    /**
     * Gets the next lectures that could be important to the user
     */
    public Cursor getNextCalendarItem() {
        return calendarDao.getNextCalendarItem();
    }

    /**
     * Gets the coordinates of the next lecture or the current running lecture,
     * if it started during the last 30 minutes
     */
    Geo getNextCalendarItemGeo() {
        Geo geo;
        try (Cursor cur = calendarDao.getNextLectureCoordinates()) {
            geo = null;
            if (cur.moveToFirst()) {
                geo = new Geo(cur.getDouble(1), cur.getDouble(2));
            }
        }
        return geo;
    }

    /**
     * Shows next lecture card if lecture is available
     *
     * @param context Context
     */
    @Override
    public void onRequestCard(Context context) {
        Cursor nextCalendarItems = getNextCalendarItem();
        if (nextCalendarItems.moveToFirst()) {
            NextLectureCard card = new NextLectureCard(context);
            card.setLectures(nextCalendarItems);
            card.apply();
        }
    }

    public static class QueryLocationsService extends IntentService {

        private static final String QUERY_LOCATIONS = "query_locations";

        public QueryLocationsService() {
            super(QUERY_LOCATIONS);
        }

        public static void loadGeo(Context c) {
            LocationManager locationManager = new LocationManager(c);
            final CalendarDao calendarDao = TcaDb.getInstance(c).calendarDao();
            final RoomLocationsDao roomLocationsDao = TcaDb.getInstance(c).roomLocationsDao();

            try (Cursor cur = calendarDao.getLecturesWithoutCoordinates()) {

                // Retrieve geo from room name
                if (cur.moveToFirst()) {
                    do {
                        String location = cur.getString(0);
                        if (location == null || location.isEmpty()) {
                            continue;
                        }
                        Optional<Geo> geo = locationManager.roomLocationStringToGeo(location);
                        if (geo.isPresent()) {
                            Utils.logv("inserted " + location + ' ' + geo);
                            roomLocationsDao.insert(new RoomLocations(location, geo.get().getLatitude(), geo.get().getLongitude()));
                        }

                    } while (cur.moveToNext());
                }
            }

            // Do sync of google calendar if necessary
            boolean syncCalendar = Utils.getInternalSettingBool(c, Const.SYNC_CALENDAR, false)
                                   && ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED;
            if (syncCalendar && new SyncManager(c).needSync(Const.SYNC_CALENDAR, TIME_TO_SYNC_CALENDAR)) {
                syncCalendar(c);
                new SyncManager(c).replaceIntoDb(Const.SYNC_CALENDAR);
            }
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            new Thread(() -> loadGeo(QueryLocationsService.this)).start();
        }
    }
}
