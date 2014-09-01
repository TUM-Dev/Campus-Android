package de.tum.in.tumcampus.models.managers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.NextLectureCard;
import de.tum.in.tumcampus.cards.ProvidesCard;
import de.tum.in.tumcampus.models.CalendarRow;
import de.tum.in.tumcampus.models.CalendarRowSet;

public class CalendarManager implements ProvidesCard {
    public static int TIME_TO_SYNC = 86400; // 1 day
    private SQLiteDatabase db;

    public CalendarManager(Context context) {
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
            Iterator itr = myKalendarList.getKalendarList().iterator();
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
            SyncManager.replaceIntoDb(db, this);
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

    @Override
    public void onRequestCard(Context context) {

        // Get first lecture which starts in the next 30 minutes
        Cursor cur = db.rawQuery("" +
                " SELECT title, dtstart, location " +
                " FROM kalendar_events " +
                " WHERE " +
                " datetime('now', 'localtime')-1800 < dtstart AND " +
                " datetime('now', 'localtime') < dtend " +
                " ORDER BY dtstart " +
                " LIMIT 1", null);

        if (cur.moveToFirst()) {
            NextLectureCard card = new NextLectureCard(context);
            card.setLecture(cur.getString(0), cur.getString(1), cur.getString(2));
            card.apply(); //TODO: if returnd false present next available
        }
        cur.close();
    }
}
