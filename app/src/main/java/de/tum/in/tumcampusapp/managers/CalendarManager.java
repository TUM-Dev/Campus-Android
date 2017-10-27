package de.tum.in.tumcampusapp.managers;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;

import com.google.common.base.Optional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.auxiliary.calendar.CalendarHelper;
import de.tum.in.tumcampusapp.auxiliary.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampusapp.cards.NextLectureCard;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.models.tumo.CalendarRow;
import de.tum.in.tumcampusapp.models.tumo.CalendarRowSet;
import de.tum.in.tumcampusapp.models.tumo.Geo;

/**
 * Calendar Manager, handles database stuff, external imports
 */
public class CalendarManager extends AbstractManager implements Card.ProvidesCard {
    private static final String[] PROJECTION = {"_id", "name"};

    private static final int TIME_TO_SYNC_CALENDAR = 604800; // 1 week

    public CalendarManager(Context context) {
        super(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS room_locations ("
                   + "title VARCHAR PRIMARY KEY, latitude VARCHAR, longitude VARCHAR)");

        db.execSQL("CREATE TABLE IF NOT EXISTS calendar ("
                   + "nr VARCHAR PRIMARY KEY, status VARCHAR, url VARCHAR, "
                   + "title VARCHAR, description VARCHAR, dtstart VARCHAR, dtend VARCHAR, "
                   + "location VARCHAR REFERENCES room_locations)");

        db.execSQL("CREATE TABLE IF NOT EXISTS widgets_timetable_blacklist ("
                   + "widget_id INTEGER, lecture_title VARCHAR, PRIMARY KEY (widget_id, lecture_title))");
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

        CalendarManager calendarManager = new CalendarManager(c);
        Date dtstart;
        Date dtend;

        // Get all calendar items from database
        try (Cursor cursor = calendarManager.getAllFromDb()) {
            while (cursor.moveToNext()) {
                // Get each table row
                //final String status = cursor.getString(1);
                final String title = cursor.getString(3);
                final String description = cursor.getString(4);
                final String strStart = cursor.getString(5);
                final String strEnd = cursor.getString(6);
                final String location = cursor.getString(7);

                try {
                    // Get the correct date and time from database
                    dtstart = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(strStart);
                    dtend = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(strEnd);

                    Calendar beginTime = Calendar.getInstance();
                    beginTime.setTime(dtstart);
                    Calendar endTime = Calendar.getInstance();
                    endTime.setTime(dtend);

                    // Get start and end time
                    long startMillis = beginTime.getTimeInMillis();
                    long endMillis = endTime.getTimeInMillis();

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

                } catch (ParseException e) {
                    Utils.log(e);
                }
            }
        }
    }

    /**
     * Returns all stored events from db
     *
     * @return Cursor with all calendar events. Columns are
     * (nr, status, url, title, description, dtstart, dtend, location)
     */
    private Cursor getAllFromDb() {
        return db.rawQuery("SELECT * FROM calendar WHERE status!='CANCEL'", null);
    }

    public Cursor getFromDbForDate(Date date) {
        // Format the requested date
        String requestedDateString = Utils.getDateString(date);

        // Fetch the data
        return db.rawQuery("SELECT * FROM calendar WHERE dtstart LIKE ? AND status!='CANCEL' ORDER BY dtstart ASC", new String[]{"%" + requestedDateString + "%"});
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
        try (Cursor cursor = db.rawQuery("SELECT * FROM calendar c WHERE dtend BETWEEN ? AND ? AND status!='CANCEL' " +
                                         "AND NOT EXISTS (SELECT * FROM widgets_timetable_blacklist WHERE widget_id=? AND lecture_title=c.title) " +
                                         "ORDER BY dtstart ASC", new String[]{from, to, String.valueOf(widgetId)})) {
            while (cursor.moveToNext()) {
                calendarEvents.add(new IntegratedCalendarEvent(cursor));
            }
        }
        return calendarEvents;
    }

    /**
     * Get current lecture from the database
     *
     * @return Database cursor (name, location, _id)
     */
    public Cursor getCurrentFromDb() {
        return db.rawQuery("SELECT title, location, nr, dtend FROM calendar WHERE datetime('now', 'localtime') BETWEEN dtstart AND dtend AND status!='CANCEL'", null);
    }

    /**
     * Checks if there are any event in the database
     *
     * @return True if there are lectures in the database, false if there is no lecture
     */
    public boolean hasLectures() {
        boolean result = false;
        try (Cursor c = db.rawQuery("SELECT nr FROM calendar", null)) {
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
        return db.rawQuery("SELECT DISTINCT c.ROWID as _id, c.title, EXISTS (" +
                           "SELECT * FROM widgets_timetable_blacklist WHERE widget_id=? AND lecture_title=c.title" +
                           ") as is_on_blacklist from calendar c GROUP BY c.title", new String[]{String.valueOf(widgetId)});
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
        db.execSQL("DELETE FROM calendar");
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

        db.execSQL("REPLACE INTO calendar (nr, status, url, title, "
                   + "description, dtstart, dtend, location) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                   new String[]{row.getNr(), row.getStatus(), row.getUrl(),
                                row.getTitle(), row.getDescription(),
                                row.getDtstart(), row.getDtend(), row.getLocation()});
    }

    /**
     * Gets the next lectures that could be important to the user
     */
    public Cursor getNextCalendarItem() {
        return db.rawQuery("SELECT title, dtstart, dtend, location FROM calendar JOIN " +
                           "(SELECT dtstart AS maxstart FROM calendar WHERE status!='CANCEL' AND datetime('now', 'localtime')<dtstart " +
                           "ORDER BY dtstart LIMIT 1) ON status!='CANCEL' AND datetime('now', 'localtime')<dtend AND dtstart<=maxstart " +
                           "ORDER BY dtend, dtstart LIMIT 4", null);
    }

    /**
     * Gets the coordinates of the next lecture or the current running lecture,
     * if it started during the last 30 minutes
     */
    Geo getNextCalendarItemGeo() {
        Geo geo;
        try (Cursor cur = db.rawQuery("SELECT r.latitude, r.longitude " +
                                      "FROM calendar c, room_locations r " +
                                      "WHERE datetime('now', 'localtime') < datetime(c.dtstart, '+1800 seconds') AND " +
                                      "datetime('now','localtime') < c.dtend AND r.title == c.location AND c.status!='CANCEL'" +
                                      "ORDER BY dtstart LIMIT 1", null)) {
            geo = null;
            if (cur.moveToFirst()) {
                geo = new Geo(cur.getDouble(0), cur.getDouble(1));
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
        Cursor rows = getNextCalendarItem();
        if (rows.moveToFirst()) {
            NextLectureCard card = new NextLectureCard(context);
            card.setLectures(rows);
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
            SQLiteDatabase db = getDb(c);

            try (Cursor cur = db.rawQuery("SELECT c.location " +
                                          "FROM calendar c LEFT JOIN room_locations r ON " +
                                          "c.location=r.title " +
                                          "WHERE r.latitude IS NULL " +
                                          "GROUP BY c.location", null)) {

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
                            db.execSQL("REPLACE INTO room_locations (title, latitude, longitude) VALUES (?, ?, ?)",
                                       new String[]{location, geo.get().getLatitude(), geo.get().getLongitude()});
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
