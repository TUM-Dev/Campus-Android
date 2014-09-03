package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.CafeteriaMenuCard;
import de.tum.in.tumcampus.cards.ProvidesCard;
import de.tum.in.tumcampus.data.LocationManager;
import de.tum.in.tumcampus.models.Cafeteria;
import de.tum.in.tumcampus.models.CafeteriaMenu;

/**
 * Cafeteria Manager, handles database stuff, external imports
 */
public class CafeteriaManager implements ProvidesCard {
    public static int TIME_TO_SYNC = 604800; // 1 week
    private final Context mContext;

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
        mContext = context;
		db = DatabaseManager.getDb(context);

        // create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS cafeterias (id INTEGER PRIMARY KEY, name VARCHAR, address VARCHAR, latitude REAL, longitude REAL)");
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

        // Update table schemata if table exists
        SharedPreferences prefs = mContext.getSharedPreferences(Const.INTERNAL_PREFS, 0);
        if(prefs.getInt(Const.CAFETERIA_DB_VERSION,1)==1) {
            db.execSQL("DROP TABLE IF EXISTS cafeterias");
            db.execSQL("CREATE TABLE IF NOT EXISTS cafeterias (id INTEGER PRIMARY KEY, name VARCHAR, address VARCHAR, latitude REAL, longitude REAL)");
            prefs.edit().putInt(Const.CAFETERIA_DB_VERSION,2).apply();
            force = true;
        }

		if (!force && !SyncManager.needSync(db, this, TIME_TO_SYNC)) {
			return;
		}

		String url = "http://lu32kap.typo3.lrz.de/mensaapp/exportDB.php";

		JSONArray jsonArray = Utils.downloadJson(url).getJSONArray(
				Const.JSON_MENSA_MENSEN);
		removeCache();


        Geocoder geo = new Geocoder(mContext);

		// write cafeterias into database, transaction = speedup
		db.beginTransaction();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
                Cafeteria mensa = getFromJson(jsonArray.getJSONObject(i));
                List<Address> list = geo.getFromLocationName(mensa.address, 1);
                if(list.size()==1) {
                    mensa.latitude = list.get(0).getLatitude();
                    mensa.longitude = list.get(0).getLongitude();
                }
				replaceIntoDb(mensa);
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

    public Cursor getAllLocationsFromDb() {
        return db
                .rawQuery(
                        "SELECT id, latitude, longitude FROM cafeterias", null);
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

    @Override
    public void onRequestCard(Context context) throws ParseException {
        CafeteriaMenuCard card = new CafeteriaMenuCard(context);
        CafeteriaMenuManager cmm = new CafeteriaMenuManager(context);
        de.tum.in.tumcampus.data.LocationManager locationManager = new LocationManager(context);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Get all available cafeterias from database
        Cursor cursor = getAllFromDb("% %");
        String cafeteriaId="", cafeteriaName = "";

        // Choose which mensa should be shown
        Location loc = locationManager.getCurrentLocation();
        if(loc!=null) {
            cafeteriaId = locationManager.getNextCafeteria(loc, this);
        }
        if (cursor.moveToFirst()) {
            do {
                final String key = cursor.getString(2);
                if (sharedPrefs.getBoolean("mensa_" + key, true) && cafeteriaId==null) {
                    cafeteriaId = key;
                    cafeteriaName = cursor.getString(0);
                    break;
                } else if(cafeteriaId!=null && key.equals(cafeteriaId)) {
                    cafeteriaName = cursor.getString(0);
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Get available dates for cafeteria menus
        Cursor cursorCafeteriaDates = cmm.getDatesFromDb();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        final int idCol = cursorCafeteriaDates.getColumnIndex(Const.ID_COLUMN);

        // Try with next available date
        cursorCafeteriaDates.moveToFirst(); // Get today or tomorrow if today is sunday e.g.
        String dateStr = cursorCafeteriaDates.getString(idCol);
        Date date = formatter.parse(dateStr);

        // If it is 3pm or later mensa has already closed so display the menu for the following day
        Calendar now = Calendar.getInstance();
        if(DateUtils.isToday(date.getTime()) && now.get(Calendar.HOUR_OF_DAY)>=15) {
            cursorCafeteriaDates.moveToNext(); // Get following day
            dateStr = cursorCafeteriaDates.getString(idCol);
            date = formatter.parse(dateStr);
        }
        cursorCafeteriaDates.close();
        Cursor cursorCafeteriaMenu = cmm.getTypeNameFromDbCard(cafeteriaId, dateStr);
        ArrayList<CafeteriaMenu> menus = new ArrayList<CafeteriaMenu>();
        if(cursorCafeteriaMenu.moveToFirst()) {
            do {
                int typeNr = 0;
                CafeteriaMenu menu = new CafeteriaMenu(Integer.parseInt(cursorCafeteriaMenu.getString(2)),
                        Integer.parseInt(cafeteriaId), date,
                        cursorCafeteriaMenu.getString(3), cursorCafeteriaMenu.getString(0), typeNr, cursorCafeteriaMenu.getString(1));

                menus.add(menu);
            } while (cursorCafeteriaMenu.moveToNext());
            card.setCardMenus(cafeteriaId, cafeteriaName, date, menus);
            card.apply();
        }
        cursorCafeteriaMenu.close();
    }
}