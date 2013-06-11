package de.tum.in.tumcampusapp.models.managers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.Cafeteria;

/**
 * Cafeteria Manager, handles database stuff, external imports
 */
public class CafeteriaManager {
	public static int TIME_TO_SYNC = 604800; // 1 week

	/**
	 * Get Cafeteria object by JSON object
	 * 
	 * Example JSON: e.g.
	 * {"id":"411","name":"Mensa Leopoldstra\u00dfe","anschrift"
	 * :"Leopoldstra\u00dfe 13a, M\u00fcnchen"}
	 * 
	 * <pre>
	 * @param json See example
	 * @return Cafeteria object
	 * @throws JSONException
	 * </pre>
	 */
	public static Cafeteria getFromJson(JSONObject json) throws JSONException {

		return new Cafeteria(json.getInt(Const.JSON_ID), json.getString(Const.JSON_NAME), json.getString(Const.JSON_ANSCHRIFT));
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
	public CafeteriaManager(Context context) {
		db = DatabaseManager.getDb(context);

		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS cafeterias (id INTEGER PRIMARY KEY, name VARCHAR, address VARCHAR)");
	}

	/**
	 * Download cafeterias from external interface (JSON)
	 * 
	 * <pre>
	 * @param force True to force download over normal sync period, else false
	 * @throws Exception
	 * </pre>
	 */
	public void downloadFromExternal(boolean force) throws Exception {

		if (!force && !SyncManager.needSync(db, this, TIME_TO_SYNC)) {
			return;
		}

		String url = "http://lu32kap.typo3.lrz.de/mensaapp/exportDB.php";

		JSONArray jsonArray = Utils.downloadJson(url).getJSONArray(Const.JSON_MENSA_MENSEN);
		removeCache();

		// write cafeterias into database, transaction = speedup
		db.beginTransaction();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				replaceIntoDb(getFromJson(jsonArray.getJSONObject(i)));
			}
			SyncManager.replaceIntoDb(db, this);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Returns all cafeterias, filterable by substring of name/address
	 * 
	 * <pre>
	 * @param filter Filter name/address by substring ("" = no filter)
	 * @return Database cursor (name, address, _id)
	 * </pre>
	 */
	public Cursor getAllFromDb(String filter) {
		return db.rawQuery("SELECT name, address, id as _id FROM cafeterias WHERE name LIKE ? OR address LIKE ? "
				+ "ORDER BY address like '%Garching%' DESC, name", new String[] { filter, filter });
	}

	/**
	 * Removes all cache items
	 */
	public void removeCache() {
		db.execSQL("DELETE FROM cafeterias");
	}

	/**
	 * Replace or Insert a cafeteria in the database
	 * 
	 * <pre>
	 * @param c Cafeteria object
	 * @throws Exception
	 * </pre>
	 */
	public void replaceIntoDb(Cafeteria c) throws Exception {
		Utils.log(c.toString());

		if (c.id <= 0) {
			throw new Exception("Invalid id.");
		}
		if (c.name.length() == 0) {
			throw new Exception("Invalid name.");
		}

		db.execSQL("REPLACE INTO cafeterias (id, name, address) VALUES (?, ?, ?)", new String[] { String.valueOf(c.id), c.name, c.address });
	}
}