package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.DateUtils;
import de.tum.in.tumcampusapp.models.cafeteria.Location;

/**
 * Location manager, handles database stuff
 */
public class OpenHoursManager extends AbstractManager {

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public OpenHoursManager(Context context) {
        super(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS locations (id INTEGER PRIMARY KEY, category VARCHAR, "
                   + "name VARCHAR, address VARCHAR, room VARCHAR, transport VARCHAR, "
                   + "hours VARCHAR, remark VARCHAR, url VARCHAR)");
    }

    /**
     * Checks if the locations table is empty
     *
     * @return true if no locations are available, else false
     */
    public boolean empty() {
        boolean result = true;
        try (Cursor c = db.rawQuery("SELECT id FROM locations LIMIT 1", null)) {
            if (c.moveToNext()) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Get all locations by category from the database
     *
     * @param category String Location category, e.g. library, cafeteria
     * @return Database cursor (name, address, room, transport, hours, remark,
     * url, _id)
     */
    public Cursor getAllHoursFromDb(String category) {
        return db.rawQuery(
                "SELECT name, address, room, transport, hours, remark, url, id as _id "
                + "FROM locations WHERE category=? ORDER BY name",
                new String[]{category});
    }

    /**
     * Get opening hours for a specific location
     *
     * @param id Location ID, e.g. 100
     * @return hours
     */
    String getHoursById(int id) {
        String result = "";
        try (Cursor c = db.rawQuery("SELECT hours FROM locations WHERE id=?",
                                    new String[]{String.valueOf(id)})) {
            if (c.moveToNext()) {
                result = c.getString(0);
            }
        }
        return result;
    }

    /**
     * Converts the opening hours into more readable format.
     * e.g. Opening in 2 hours.
     * HINT: Currently only works for cafeterias, and institutions
     * that have Mo-Do xx-yy.yy, Fr aa-bb and Mo-Fr xx-yy format
     *
     * @param context Context
     * @param id      Location ID, e.g. 100
     * @param date    Relative date
     * @return Readable opening string
     */
    public String getHoursByIdAsString(Context context, int id, Date date) {
        String result = this.getHoursById(id);

        //Check which week day we have
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        //Split up the data string from the database with regex which has the format: "Mo-Do 11-14, Fr 11-13.45" or "Mo-Fr 9-20"
        Matcher m = Pattern.compile("([a-z]{2}?)[-]?([a-z]{2}?)? ([0-9]{1,2}(?:[\\.][0-9]{2}?)?)-([0-9]{1,2}(?:[\\.][0-9]{2}?)?)", Pattern.CASE_INSENSITIVE)
                           .matcher(result);

        //Capture groups for: Mo-Do 9-21.30
        //#0	Mo-Do 9-21.30
        //#1	Mo
        //#2	Do
        //#3	9
        //#4	21.30

        //Find the first part
        String[] time = new String[2];
        if (m.find()) {
            //We are currently in Mo-Do/Fr, when this weekday is in that range we have our result or we check if the current range is valid for fridays also
            if (dayOfWeek <= Calendar.THURSDAY || m.group(2)
                                                   .equalsIgnoreCase("fr")) {
                time[0] = m.group(3);
                time[1] = m.group(4);
            } else {
                //Otherwise we need to move to the next match
                if (m.find()) {
                    //Got a match, data should be in capture groups 3/4
                    time[0] = m.group(3);
                    time[1] = m.group(4);
                } else {
                    //No match found, return
                    return "";
                }
            }
        } else {
            //No match found, return
            return "";
        }

        //Convert time to workable calender objects
        Calendar now = Calendar.getInstance();
        Calendar opens = strToCal(date, time[0]);
        Calendar closes = strToCal(date, time[1]);

        //Check the relativity
        Calendar relativeTo;
        int relation;
        if (opens.after(now)) {
            relation = R.string.opens;
            relativeTo = opens;
        } else if (closes.after(now)) {
            relation = R.string.closes;
            relativeTo = closes;
        } else {
            relation = R.string.closed;
            relativeTo = closes;
        }

        //Get the relative string
        String relStr = DateUtils.getFutureTime(relativeTo.getTime(), context);

        //Return an assembly
        return context.getString(relation) + " " + relStr.substring(0, 1)
                                                         .toLowerCase(Locale.getDefault()) + relStr.substring(1);

    }

    private static Calendar strToCal(Date date, String time) {
        Calendar opens = Calendar.getInstance();
        opens.setTime(date);
        if (time.contains(".")) {
            int hour = Integer.parseInt(time.substring(0, time.indexOf('.')));
            int min = Integer.parseInt(time.substring(time.indexOf('.') + 1));
            opens.set(Calendar.HOUR_OF_DAY, hour);
            opens.set(Calendar.MINUTE, min);
        } else {
            opens.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time));
            opens.set(Calendar.MINUTE, 0);
        }
        return opens;
    }

    /**
     * Replaces a location in the database
     *
     * @param l Location object
     */
    public void replaceIntoDb(Location l) {
        if (l.getId() <= 0) {
            throw new IllegalArgumentException("Invalid id.");
        }
        if (l.getName()
             .isEmpty()) {
            throw new IllegalArgumentException("Invalid name.");
        }
        db.execSQL("REPLACE INTO locations (id, category, name, address, room, "
                   + "transport, hours, remark, url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                   new String[]{String.valueOf(l.getId()), l.getCategory(), l.getName(),
                                l.getAddress(), l.getRoom(), l.getTransport(), l.getHours(), l.getRemark(),
                                l.getUrl()});
    }
}