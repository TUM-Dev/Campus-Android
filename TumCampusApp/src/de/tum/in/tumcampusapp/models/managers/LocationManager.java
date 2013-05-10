package de.tum.in.tumcampusapp.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.Location;

/**
 * Location manager, handles database stuff
 */
public class LocationManager {

	/**
	 * Database connection
	 */
	private SQLiteDatabase db;

	/**
	 * Constructor, open/create database, create table if necessary
	 * 
	 * <pre>
	 * @param context Context
	 * </pre>
	 */
	public LocationManager(Context context) {
		db = DatabaseManager.getDb(context);

		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS locations (id INTEGER PRIMARY KEY, category VARCHAR, "
				+ "name VARCHAR, address VARCHAR, room VARCHAR, transport VARCHAR, " + "hours VARCHAR, remark VARCHAR, url VARCHAR)");
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
	 * <pre>
	 * @param category String Location category, e.g. library, cafeteria
	 * @return Database cursor (name, address, room, transport, hours, remark, 
	 * 		url, _id)
	 * </pre>
	 */
	public Cursor getAllHoursFromDb(String category) {
		return db.rawQuery("SELECT name, address, room, transport, hours, remark, url, id as _id " + "FROM locations WHERE category=? ORDER BY name",
				new String[] { category });
	}

	/**
	 * Get opening hours for a specific location
	 * 
	 * <pre>
	 * @param id String Location ID, e.g. 100
	 * @return String hours
	 * </pre>
	 */
	public String getHoursById(String id) {
		Cursor c = db.rawQuery("SELECT hours FROM locations WHERE id=?", new String[] { id });

		if (c.moveToNext()) {
			return c.getString(0);
		}
		return "";
	}

	/**
	 * Replaces a location in the database
	 * 
	 * <pre>
	 * @param l Location object
	 * @throws Exception
	 * </pre>
	 */
	public void replaceIntoDb(Location l) throws Exception {
		Utils.log(l.toString());

		if (l.id <= 0) {
			throw new Exception("Invalid id.");
		}
		if (l.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		db.execSQL("REPLACE INTO locations (id, category, name, address, room, " + "transport, hours, remark, url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new String[] { String.valueOf(l.id), l.category, l.name, l.address, l.room, l.transport, l.hours, l.remark, l.url });
	}
}