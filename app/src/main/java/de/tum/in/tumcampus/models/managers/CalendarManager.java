package de.tum.in.tumcampus.models.managers;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.CalendarMapper;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.NextLectureCard;
import de.tum.in.tumcampus.models.CalendarRow;
import de.tum.in.tumcampus.models.CalendarRowSet;

public class CalendarManager implements Card.ProvidesCard {
    private static final String[] projection = new String[] { "_id", "name" };

    private static final int TIME_TO_SYNC_CALENDAR = 604800000; // 1 week
    public static int TIME_TO_SYNC = 86400; // 1 day

    private final Context mContext;
    private SQLiteDatabase db;

    public CalendarManager(Context context) {
        mContext = context;
        db = DatabaseManager.getDb(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS kalendar_events ("
                + "nr VARCHAR PRIMARY KEY, status VARCHAR, url VARCHAR, "
                + "title VARCHAR, description VARCHAR, dtstart VARCHAR, dtend VARCHAR, "
                + "location VARCHAR, longitude VARCHAR, latitude VARCHAR)");

        // Create a new Synch table
        new SyncManager(context);
    }

    /**
     * Returns all stored events from db
     *
     * @return
     */
    public Cursor getAllFromDb() {
        return db.rawQuery("SELECT * FROM kalendar_events", null);
    }

    public Cursor getFromDbForDate(Date date) {
        // Format the requested date
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        String requestedDateString = sf.format(date);

        // Fetch the data
        return db.rawQuery("SELECT * FROM kalendar_events WHERE dtstart LIKE ? ORDER BY dtstart ASC", new String[]{"%" + requestedDateString + "%"});
    }

    /**
     * Get all lecture items from the database
     *
     * @return Database cursor (name, location, _id)
     */
    public Cursor getCurrentFromDb() {
        return db
                .rawQuery(
                        "SELECT title, location, nr "
                                + "FROM kalendar_events WHERE datetime('now', 'localtime') BETWEEN dtstart AND dtend",
                        null);
    }

    /**
     * Checks if there are any event in the database
     *
     * @return
     */
    public boolean hasLectures() {
        boolean result = false;
        Cursor c = db.rawQuery("SELECT nr FROM kalendar_events", null);
        if (c.moveToNext()) {
            result = true;
        }
        c.close();
        return result;
    }

    public void importKalendar(String rawResponse) {
        // reader for xml
        Serializer serializer = new Persister();

        // KalendarRowSet will contain list of events in KalendarRow
        CalendarRowSet myKalendarList = new CalendarRowSet();

        myKalendarList.setKalendarList(new ArrayList<CalendarRow>());

        try {
            // reading xml
            myKalendarList = serializer.read(CalendarRowSet.class, rawResponse);
            List<CalendarRow> myKalendar = myKalendarList.getKalendarList();
            if(myKalendar!=null) {
                Iterator itr = myKalendar.iterator();
                while (itr.hasNext()) {
                    CalendarRow row = (CalendarRow) itr.next();
                    // insert into database
                    try {
                        replaceIntoDb(row);
                    } catch (Exception e) {
                        boolean success = false;
                        Log.d("SIMPLEXML", "Error in field: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            SyncManager.replaceIntoDb(db, this);

            // Do sync of google calendar if neccessary
            SharedPreferences prefs = mContext.getSharedPreferences(Const.INTERNAL_PREFS, 0);
            if(prefs.getBoolean(Const.SYNC_CALENDAR, false) && SyncManager.needSync(db, Const.SYNC_CALENDAR, TIME_TO_SYNC_CALENDAR)) {
                syncCalendar(mContext);
            }
        } catch (Exception e) {
            boolean success = false;
            Log.d("SIMPLEXML", "wont work: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean needsSync() {
        return SyncManager.needSync(db, this, TIME_TO_SYNC);
    }

    /**
     * Removes all cache items
     */
    public void removeCache() {
        db.execSQL("DELETE FROM kalendar_events");
    }

    public void replaceIntoDb(CalendarRow row) throws Exception {
        Utils.log(row.toString());

        if (row.getNr().length() == 0)
            throw new Exception("Invalid id.");

        if (row.getTitle().length() == 0)
            throw new Exception("Invalid lecture Title.");

        if (row.getGeo() != null)
            db.execSQL(
                    "REPLACE INTO kalendar_events (nr, status, url, title, "
                            + "description, dtstart, dtend, location, longitude, latitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new String[]{row.getNr(), row.getStatus(), row.getUrl(),
                            row.getTitle(), row.getDescription(),
                            row.getDtstart(), row.getDtend(),
                            row.getLocation(), row.getGeo().getLongitude(),
                            row.getGeo().getLatitude()});
        else
            db.execSQL(
                    "REPLACE INTO kalendar_events (nr, status, url, title, "
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
    public static void syncCalendar(Context c) {
        // Deleting earlier calendar created by TUM Campus App
        deleteLocalCalendar(c);
        Uri uri = addLocalCalendar(c);
        addEvents(c, uri);
        SyncManager.replaceIntoDb(DatabaseManager.getDb(c), Const.SYNC_CALENDAR);
    }


    private static  Uri addLocalCalendar(Context c) {
        ContentResolver crv = c.getContentResolver();
        Calendar calendar = Calendar.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        CalendarMapper calendarMapper = new CalendarMapper(c.getString(R.string.calendar_account_name), c.getString(R.string.calendar_display_name), preferences);
        return calendarMapper.addCalendar(calendar, crv);
    }

    /**
     * Deletes a local Google calendar
     *
     * @return The calendars id
     */
    public static int deleteLocalCalendar(Context c) {
        ContentResolver crv = c.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        return crv.delete(uri, " account_name = '" + c.getString(R.string.calendar_account_name) + "'", null);
    }

    @SuppressLint("InlinedApi")
    private static void addEvents( Context c, Uri uri) {
        // Get ID
        ContentResolver contentResolver = c.getContentResolver();
        Cursor cursor2 = contentResolver.query(uri, projection, null, null, null);
        String id = "0";
        while (cursor2.moveToNext()) {
            id = cursor2.getString(0);
        }

        CalendarManager calendarManager = new CalendarManager(c);
        Date dtstart, dtend;

        // Get all calendar items from database
        Cursor cursor = calendarManager.getAllFromDb();
        while (cursor.moveToNext()) {
            // Get each table row
            final String status = cursor.getString(1);
            final String title = cursor.getString(3);
            final String description = cursor.getString(4);
            final String strstart = cursor.getString(5);
            final String strend = cursor.getString(6);
            final String location = cursor.getString(7);

            if (!status.equals("CANCEL")) {
                try {
                    // Get the correct date and time from database
                    dtstart = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(strstart);
                    dtend = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(strend);

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
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets the next lecture or the current running lecture,
     * if it started during the last 30 minutes
     * */
    public CalendarRow getNextCalendarItem() {
        Cursor cur = db.rawQuery("" +
                " SELECT title, dtstart, location " +
                " FROM kalendar_events " +
                " WHERE " +
                " datetime('now', 'localtime')-1800 < dtstart AND " +
                " datetime('now', 'localtime') < dtend " +
                " ORDER BY dtstart " +
                " LIMIT 1", null);

        CalendarRow row = null;
        if (cur.moveToFirst()) {
            row = new CalendarRow();
            row.setTitle(cur.getString(0));
            row.setDtstart(cur.getString(1));
            row.setLocation(cur.getString(2));
        }
        cur.close();
        return row;
    }

    @Override
    public void onRequestCard(Context context) {
        CalendarRow row = getNextCalendarItem();
        if (row!=null) {
            NextLectureCard card = new NextLectureCard(context);
            card.setLecture(row.getTitle(), row.getDtstart(), row.getDtend());
            card.apply();
        }
    }
}
