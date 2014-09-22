package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.Location;

/**
 * Location manager, handles database stuff
 */
public class OpenHoursManager {

	/** Database connection */
	private final SQLiteDatabase db;

	/**
	 * Constructor, open/create database, create table if necessary
	 *
	 * @param context Context
	 */
	public OpenHoursManager(Context context) {
		db = DatabaseManager.getDb(context);

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
		Cursor c = db.rawQuery("SELECT id FROM locations LIMIT 1", null);
		if (c.moveToNext()) {
			result = false;
		}
		c.close();
		return result;
	}

	/**
	 * Get all locations by category from the database
	 *
	 * @param category String Location category, e.g. library, cafeteria
	 * @return Database cursor (name, address, room, transport, hours, remark, 
	 * 		url, _id)
	 */
	public Cursor getAllHoursFromDb(String category) {
		return db.rawQuery(
				"SELECT name, address, room, transport, hours, remark, url, id as _id "
						+ "FROM locations WHERE category=? ORDER BY name",
				new String[] { category });
	}

	/**
	 * Get opening hours for a specific location
	 *
	 * @param id Location ID, e.g. 100
	 * @return hours
	 */
    String getHoursById(int id) {
		String result = "";
		Cursor c = db.rawQuery("SELECT hours FROM locations WHERE id=?",
				new String[] { ""+id });

		if (c.moveToNext()) {
			result = c.getString(0);
		}
		c.close();
		return result;
	}

    /**
     * Converts the opening hours into more readable format.
     * e.g. Opening in 2 hours.
     * HINT: Currently only works for cafeterias, and institutions
     * that have Mo-Do xx-yy.yy, Fr aa-bb and Mo-Fr xx-yy format
     *
     * @param context Context
     * @param id Location ID, e.g. 100
     * @param date Relative date
     * @return Readable opening string
     */
    public String getHoursByIdAsString(Context context, int id, Date date) {
        String result = getHoursById(id);
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String interval;
        if(result.startsWith("Mo-Do") && dayOfWeek<=Calendar.THURSDAY) {
            interval = result.substring(result.indexOf("Do")+3,result.indexOf(","));
        } else {
            interval = result.substring(result.indexOf("Fr")+3);
        }
        String[] time = interval.split("-");

        Calendar now = Calendar.getInstance();
        Calendar opens = strToCal(date, time[0]);
        Calendar closes = strToCal(date, time[1]);
        Calendar relativeTo;
        int relation;
        if(opens.after(now)) {
            relation = R.string.opens;
            relativeTo = opens;
        } else if(closes.after(now)) {
            relation = R.string.closes;
            relativeTo = closes;
        } else {
            relation = R.string.closed;
            relativeTo = closes;
        }
        String relStr = DateUtils.getRelativeTimeSpanString(
                relativeTo.getTimeInMillis(),
                now.getTimeInMillis(),
                DateUtils.MINUTE_IN_MILLIS).toString();
        return context.getString(relation)+" "+relStr.substring(0,1).toLowerCase()+relStr.substring(1);

    }

    private Calendar strToCal(Date date, String time) {
        Calendar opens = Calendar.getInstance();
        opens.setTime(date);
        if(time.contains(".")) {
            int hour = Integer.parseInt(time.substring(0, time.indexOf(".")));
            int min = Integer.parseInt(time.substring(time.indexOf(".") + 1));
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
	 * @throws Exception
	 */
	public void replaceIntoDb(Location l) throws Exception {
		Utils.log(l.toString());

		if (l.id <= 0) {
			throw new Exception("Invalid id.");
		}
		if (l.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		db.execSQL("REPLACE INTO locations (id, category, name, address, room, "
						+ "transport, hours, remark, url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new String[] { String.valueOf(l.id), l.category, l.name,
						l.address, l.room, l.transport, l.hours, l.remark,
						l.url });
	}
}