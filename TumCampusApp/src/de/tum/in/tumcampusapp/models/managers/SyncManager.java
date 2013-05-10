package de.tum.in.tumcampusapp.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Sync Manager, tracks last successful syncs
 */
public class SyncManager {

	/**
	 * Checks if a new sync is needed or if data is up-to-date
	 * 
	 * <pre>
	 * @param db Database connection
	 * @param obj Gives class name as sync ID
	 * @param seconds Sync period, e.g. 86400 for 1 day
	 * @return true if sync is needed, else false
	 * </pre>
	 */
	public static boolean needSync(SQLiteDatabase db, Object obj, int seconds) {
		return needSync(db, obj.getClass().getName(), seconds);
	}

	/**
	 * Checks if a new sync is needed or if data is up-to-date
	 * 
	 * <pre>
	 * @param db Database connection
	 * @param id Sync-ID (derived by originator class name)
	 * @param seconds Sync period, e.g. 86400 for 1 day
	 * @return true if sync is needed, else false
	 * </pre>
	 */
	public static boolean needSync(SQLiteDatabase db, String id, int seconds) {
		boolean result = true;
		Cursor c = db.rawQuery("SELECT lastSync FROM syncs WHERE lastSync > datetime('now', '-" + seconds + " second') AND id=?", new String[] { id });
		if (c.getCount() == 1) {
			result = false;
		}
		c.close();
		return result;
	}

	/**
	 * Replace or Insert a successful sync event in the database
	 * 
	 * <pre>
	 * @param db Database connection
	 * @param obj Gives class name as sync ID
	 * @throws Exception
	 * </pre>
	 */
	public static void replaceIntoDb(SQLiteDatabase db, Object obj) {
		replaceIntoDb(db, obj.getClass().getName());
	}

	/**
	 * Replace or Insert a successful sync event in the database
	 * 
	 * <pre>
	 * @param db Database connection
	 * @param id Sync-ID (derived by originator class name)
	 * </pre>
	 */
	public static void replaceIntoDb(SQLiteDatabase db, String id) {
		Utils.log(id);

		if (id.length() == 0) {
			return;
		}
		db.execSQL("REPLACE INTO syncs (id, lastSync) VALUES (?, datetime())", new String[] { id });
	}

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
	public SyncManager(Context context) {
		db = DatabaseManager.getDb(context);

		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS syncs (id VARCHAR PRIMARY KEY, lastSync VARCHAR)");
	}

	/**
	 * Removes all items from database
	 */
	public void deleteFromDb() {
		db.execSQL("DELETE FROM syncs");
	}
}