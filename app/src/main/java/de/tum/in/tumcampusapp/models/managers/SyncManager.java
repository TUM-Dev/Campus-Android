package de.tum.in.tumcampusapp.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Sync Manager, tracks last successful syncs
 */
public class SyncManager extends AbstractManager {

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public SyncManager(Context context) {
		super(context);
		// create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS syncs (id VARCHAR PRIMARY KEY, lastSync VARCHAR)");
    }

	/**
	 * Checks if a new sync is needed or if data is up-to-date
	 *
	 * @param db Database connection
	 * @param obj Gives class name as sync ID
	 * @param seconds Sync period, e.g. 86400 for 1 day
	 * @return true if sync is needed, else false
	 */
	public static boolean needSync(SQLiteDatabase db, Object obj, int seconds) {
		return needSync(db, obj.getClass().getName(), seconds);
	}

	/**
	 * Checks if a new sync is needed or if data is up-to-date
	 *
	 * @param db Database connection
	 * @param id Sync-ID (derived by originator class name)
	 * @param seconds Sync period, e.g. 86400 for 1 day
	 * @return true if sync is needed, else false
	 */
	public static boolean needSync(SQLiteDatabase db, String id, int seconds) {
		boolean result = true;

		try {
			Cursor c = db.rawQuery(
					"SELECT lastSync FROM syncs WHERE lastSync > datetime('now', '-"
							+ seconds + " second') AND id=?", new String[] { id });
			if (c.getCount() == 1) {
				result = false;
			}
			c.close();
		} catch (SQLiteException e) {
			if (e.getMessage().contains("no such table")) {
				Utils.log("Error selecting table syncs because it doesn't exist!");
				return true;
			}
		}
		return result;
	}

	/**
	 * Replace or Insert a successful sync event in the database
	 *
	 * @param db Database connection
	 * @param obj Gives class name as sync ID
	 */
	public static void replaceIntoDb(SQLiteDatabase db, Object obj) {
		replaceIntoDb(db, obj.getClass().getName());
	}

	/**
	 * Replace or Insert a successful sync event in the database
	 *
	 * @param db Database connection
	 * @param id Sync-ID (derived by originator class name)
	 */
	public static void replaceIntoDb(SQLiteDatabase db, String id) {
		Utils.log(id);

		if (id.isEmpty()) {
			return;
		}
		db.execSQL("REPLACE INTO syncs (id, lastSync) VALUES (?, datetime())",
				new String[] { id });
	}

	/**
	 * Removes all items from database
	 */
	public void deleteFromDb() {
		db.execSQL("DELETE FROM syncs");
	}
}