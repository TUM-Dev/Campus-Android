package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.CafeteriaMenuCard;
import de.tum.in.tumcampus.models.Cafeteria;
import de.tum.in.tumcampus.models.CafeteriaMenu;

/**
 * Cafeteria Manager, handles database stuff, external imports
 */
public class CafeteriaManager implements ProvidesCard {
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

		return new Cafeteria(json.getInt(Const.JSON_ID),
				json.getString(Const.JSON_NAME),
				json.getString(Const.JSON_ANSCHRIFT));
	}

	/**
	 * Database connection
	 */
	private final SQLiteDatabase db;

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

        new SyncManager(context);
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

		JSONArray jsonArray = Utils.downloadJson(url).getJSONArray(
				Const.JSON_MENSA_MENSEN);
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
		return db
				.rawQuery(
						"SELECT name, address, id as _id FROM cafeterias WHERE name LIKE ? OR address LIKE ? "
								+ "ORDER BY address like '%Garching%' DESC, name",
						new String[] { filter, filter });
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

		db.execSQL(
				"REPLACE INTO cafeterias (id, name, address) VALUES (?, ?, ?)",
				new String[] { String.valueOf(c.id), c.name, c.address });
	}

    // TODO: Make this more secure (really handle Exceptions) and faster
    @Override
    public void onRequestCard(Context context) {
        try {
            downloadFromExternal(true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        CafeteriaMenuCard card = new CafeteriaMenuCard();
        CafeteriaMenuManager cmm = new CafeteriaMenuManager(context);
        try {
            cmm.downloadFromExternal(true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Get all available cafeterias from database
        Cursor cursor = getAllFromDb("% %");
        String cafeteriaId="", cafeteriaName = "";

        //TODO: Make selection of shown cafeteria more intelligent (use location, timetable)
        if (cursor.moveToFirst()) {
            do {
                final String key = cursor.getString(2);
                if (sharedPrefs.getBoolean("mensa_"+key, true)) {
                    cafeteriaId = key;
                    cafeteriaName=cursor.getString(0);
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        Cursor cursorCafeteriaDates = cmm.getDatesFromDb();

        //TODO: Make selection of date more intelligent (use next day if called in the evening)
        cursorCafeteriaDates.moveToFirst(); // Get today
        String date = cursorCafeteriaDates.getString(cursorCafeteriaDates.getColumnIndex(Const.ID_COLUMN));

        Cursor cursorCafeteriaMenu = cmm.getTypeNameFromDbCard(cafeteriaId, date);
        ArrayList<CafeteriaMenu> menus = new ArrayList<CafeteriaMenu>();
        cursorCafeteriaMenu.moveToFirst();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date dateStr = new Date();
        try {
            dateStr = formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        do {
            int typeNr=0;
            CafeteriaMenu menu = new CafeteriaMenu(Integer.parseInt(cursorCafeteriaMenu.getString(2)),
                    Integer.parseInt(cafeteriaId), dateStr,
                    cursorCafeteriaMenu.getString(3), cursorCafeteriaMenu.getString(0), typeNr, cursorCafeteriaMenu.getString(1));

            menus.add(menu);
        } while(cursorCafeteriaMenu.moveToNext());
        card.setCardMenus(cafeteriaId, cafeteriaName, dateStr, menus);
        card.apply();
    }
}