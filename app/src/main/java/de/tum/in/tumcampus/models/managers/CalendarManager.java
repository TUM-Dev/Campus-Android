package de.tum.in.tumcampus.models.managers;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.CalendarHelper;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.NextLectureCard;
import de.tum.in.tumcampus.models.CalendarRow;
import de.tum.in.tumcampus.models.CalendarRowSet;
import de.tum.in.tumcampus.models.Geo;

/**
 * Calendar Manager, handles database stuff, external imports
 */
public class CalendarManager implements Card.ProvidesCard {
    private static final String[] projection = new String[]{"_id", "name"};

    private static final int TIME_TO_SYNC_CALENDAR = 604800; // 1 week

    private final Context mContext;
    private final SQLiteDatabase db;

    public CalendarManager(Context context) {
        mContext = context;
        db = DatabaseManager.getDb(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS room_locations ("
                + "title VARCHAR PRIMARY KEY, latitude VARCHAR, longitude VARCHAR)");

        db.execSQL("CREATE TABLE IF NOT EXISTS calendar ("
                + "nr VARCHAR PRIMARY KEY, status VARCHAR, url VARCHAR, "
                + "title VARCHAR, description VARCHAR, dtstart VARCHAR, dtend VARCHAR, "
                + "location VARCHAR REFERENCES room_locations)");

        // Create a new sync table
        new SyncManager(context);
    }

    /**
     * Returns all stored events from db
     *
     * @return Cursor with all calendar events. Columns are
     * (nr, status, url, title, description, dtstart, dtend, location)
     */
    Cursor getAllFromDb() {
        return db.rawQuery("SELECT * FROM calendar WHERE status!=\"CANCEL\"", null);
    }

    public Cursor getFromDbForDate(Date date) {
        // Format the requested date
        String requestedDateString = Utils.getDateString(date);

        // Fetch the data
        return db.rawQuery("SELECT * FROM calendar WHERE dtstart LIKE ? AND status!=\"CANCEL\" ORDER BY dtstart ASC", new String[]{"%" + requestedDateString + "%"});
    }

    /**
     * Get current lecture from the database
     *
     * @return Database cursor (name, location, _id)
     */
    public Cursor getCurrentFromDb() {
        return db.rawQuery("SELECT title, location, nr FROM calendar WHERE datetime('now', 'localtime') BETWEEN dtstart AND dtend AND status!=\"CANCEL\"", null);
    }

    /**
     * Checks if there are any event in the database
     *
     * @return True if there are lectures in the database, false if there is no lecture
     */
    public boolean hasLectures() {
        boolean result = false;
        Cursor c = db.rawQuery("SELECT nr FROM calendar", null);
        if (c.moveToNext()) {
            result = true;
        }
        c.close();
        return result;
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
        SyncManager.replaceIntoDb(DatabaseManager.getDb(mContext), Const.SYNC_CALENDAR_IMPORT);
    }

    /**
     * Removes all cache items
     */
    public void removeCache() {
        db.execSQL("DELETE FROM calendar");
    }

    void replaceIntoDb(CalendarRow row) throws Exception {
        if (row.getNr().length() == 0)
            throw new Exception("Invalid id.");

        if (row.getTitle().length() == 0)
            throw new Exception("Invalid lecture Title.");

        db.execSQL("REPLACE INTO calendar (nr, status, url, title, "
                        + "description, dtstart, dtend, location) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                new String[]{row.getNr(), row.getStatus(), row.getUrl(),
                        row.getTitle(), row.getDescription(),
                        row.getDtstart(), row.getDtend(), row.getLocation()});
    }

    /**
     * Replaces the current TUM_CAMPUS_APP calendar with a new version
     *
     * @param c Context
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
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
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static int deleteLocalCalendar(Context c) {
        return CalendarHelper.deleteCalendar(c);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void addEvents(Context c, Uri uri) {
        // Get ID
        ContentResolver contentResolver = c.getContentResolver();
        Cursor cursor2 = contentResolver.query(uri, projection, null, null, null);
        String id = "0";
        while (cursor2.moveToNext()) {
            id = cursor2.getString(0);
        }
        cursor2.close();

        CalendarManager calendarManager = new CalendarManager(c);
        Date dtstart, dtend;

        // Get all calendar items from database
        Cursor cursor = calendarManager.getAllFromDb();
        while (cursor.moveToNext()) {
            // Get each table row
            final String status = cursor.getString(1);
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

    /**
     * Gets the next lectures that could be important to the user
     */
    public Cursor getNextCalendarItem() {
        return db.rawQuery("SELECT title, dtstart, dtend, location FROM calendar JOIN " +
                "(SELECT dtstart AS maxstart FROM calendar WHERE status!=\"CANCEL\" AND datetime('now', 'localtime')<dtstart " +
                "ORDER BY dtstart LIMIT 1) ON status!=\"CANCEL\" AND datetime('now', 'localtime')<dtend AND dtstart<=maxstart " +
                "ORDER BY dtend, dtstart LIMIT 4", null);
    }

    /**
     * Gets the coordinates of the next lecture or the current running lecture,
     * if it started during the last 30 minutes
     */
    public Geo getNextCalendarItemGeo() {
        Cursor cur = db.rawQuery("SELECT r.latitude, r.longitude " +
                "FROM calendar c, room_locations r " +
                "WHERE datetime('now', 'localtime') < datetime(c.dtstart, '+1800 seconds') AND " +
                "datetime('now','localtime') < c.dtend AND r.title == c.location AND c.status!=\"CANCEL\"" +
                "ORDER BY dtstart LIMIT 1", null);

        Geo geo = null;
        if (cur.moveToFirst()) {
            geo = new Geo(cur.getDouble(0), cur.getDouble(1));
        }
        cur.close();
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

        @Override
        protected void onHandleIntent(Intent intent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadGeo(QueryLocationsService.this);
                }
            }).start();
        }

        public static void loadGeo(Context c) {
            SQLiteDatabase db = DatabaseManager.getDb(c);
            LocationManager locationManager = new LocationManager(c);

            Cursor cur = db.rawQuery("SELECT c.location " +
                    "FROM calendar c LEFT JOIN room_locations r ON " +
                    "c.location=r.title " +
                    "WHERE r.latitude IS NULL " +
                    "GROUP BY c.location", null);

            // Retrieve geo from room name
            if(cur.moveToFirst()) {
                do {
                    String location = cur.getString(0);
                    if(location != null && !location.isEmpty()) {
                        Geo geo = locationManager.roomLocationStringToGeo(location);
                        if (geo != null) {
                            Utils.logv("inserted "+location+" "+geo);
                            db.execSQL("REPLACE INTO room_locations (title, latitude, longitude) VALUES (?, ?, ?)",
                                    new String[]{location, geo.getLatitude(), geo.getLongitude()});
                        }
                    }
                } while(cur.moveToNext());
            }
            cur.close();

            // Do sync of google calendar if necessary
            boolean syncCalendar = Utils.getInternalSettingBool(c, Const.SYNC_CALENDAR, false);
            if (syncCalendar && SyncManager.needSync(db, Const.SYNC_CALENDAR, TIME_TO_SYNC_CALENDAR)) {
                syncCalendar(c);
                SyncManager.replaceIntoDb(db, Const.SYNC_CALENDAR);
            }
        }
    }
}
