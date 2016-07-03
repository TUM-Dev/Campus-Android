package de.tum.in.tumcampusapp.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.CafeteriaMenuCard;
import de.tum.in.tumcampusapp.cards.Card;
import de.tum.in.tumcampusapp.models.Cafeteria;
import de.tum.in.tumcampusapp.models.CafeteriaMenu;

/**
 * Cafeteria Manager, handles database stuff, external imports
 */
public class CafeteriaManager extends AbstractManager implements Card.ProvidesCard {
    private static final int TIME_TO_SYNC = 604800; // 1 week

    public static final String CAFETERIAS_URL = "https://tumcabe.in.tum.de/Api/mensen";

    /**
     * Get Cafeteria object by JSON object
     * <p>
     * Example JSON: e.g.
     * {"mensa":"4", "id":"411","name":"Mensa Leopoldstra\u00dfe","anschrift"
     * :"Leopoldstra\u00dfe 13a, M\u00fcnchen", "latitude":0.0000, "longitude":0.0000}
     *
     * @param json See example
     * @return Cafeteria object
     * @throws JSONException
     */
    private static Cafeteria getFromJson(JSONObject json) throws JSONException {
        return new Cafeteria(json.getInt(Const.JSON_ID),
                json.getString(Const.JSON_NAME),
                json.getString(Const.JSON_ADDRESS),
                json.getDouble(Const.JSON_LATITUDE),
                json.getDouble(Const.JSON_LONGITUDE));
    }

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public CafeteriaManager(Context context) {
        super(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS cafeterias (id INTEGER PRIMARY KEY, name VARCHAR, address VARCHAR, latitude REAL, longitude REAL)");
    }

    /**
     * Download cafeterias from external interface (JSON)
     *
     * @param force True to force download over normal sync period, else false
     * @throws Exception
     */
    public void downloadFromExternal(boolean force) throws Exception {
        // Update table schemata if table exists
        if (!force && !SyncManager.needSync(db, this, TIME_TO_SYNC)) {
            return;
        }

        JSONArray jsonArray = new NetUtils(mContext).downloadJsonArray(CAFETERIAS_URL, CacheManager.VALIDITY_ONE_MONTH, false);
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
     * Returns all cafeterias
     *
     * @return Database cursor (id, name, address, latitude, longitude)
     */
    public Cursor getAllFromDb() {
        return db.query("cafeterias", null, null, null, null, null, null);
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
     * @param c Cafeteria object
     * @throws Exception
     */
    void replaceIntoDb(Cafeteria c) throws Exception {
        if (c.id <= 0) {
            throw new Exception("Invalid id.");
        }
        if (c.name.isEmpty()) {
            throw new Exception("Invalid name.");
        }

        db.execSQL("REPLACE INTO cafeterias (id, name, address, latitude, longitude) VALUES (?, ?, ?, ?, ?)",
                new String[]{String.valueOf(c.id), c.name, c.address, Double.toString(c.latitude), Double.toString(c.longitude)});
    }

    /**
     * Shows card for the best matching cafeteria.
     *
     * @param context Context
     * @see LocationManager#getCafeteria()
     */
    @Override
    public void onRequestCard(Context context) {
        CafeteriaMenuCard card = new CafeteriaMenuCard(context);
        CafeteriaMenuManager cmm = new CafeteriaMenuManager(context);
        LocationManager locationManager = new LocationManager(context);

        // Get all available cafeterias from database
        Cursor cursor = getAllFromDb();
        String cafeteriaName = "";

        // Choose which mensa should be shown
        int cafeteriaId = locationManager.getCafeteria();
        if (cafeteriaId == -1) {
            return;
        }

        if (cursor.moveToFirst()) {
            do {
                final int key = cursor.getInt(0);
                if (key == cafeteriaId) {
                    cafeteriaName = cursor.getString(1);
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Get available dates for cafeteria menus
        Cursor cursorCafeteriaDates = cmm.getDatesFromDb();
        final int idCol = cursorCafeteriaDates.getColumnIndex(Const.ID_COLUMN);

        // Try with next available date
        cursorCafeteriaDates.moveToFirst(); // Get today or tomorrow if today is sunday e.g.
        String dateStr = cursorCafeteriaDates.getString(idCol);
        Date date = Utils.getDate(dateStr);

        // If it is 3pm or later mensa has already closed so display the menu for the following day
        Calendar now = Calendar.getInstance();
        if (DateUtils.isToday(date.getTime()) && now.get(Calendar.HOUR_OF_DAY) >= 15) {
            cursorCafeteriaDates.moveToNext(); // Get following day
            dateStr = cursorCafeteriaDates.getString(idCol);
            date = Utils.getDate(dateStr);
        }
        cursorCafeteriaDates.close();

        List<CafeteriaMenu> menus = cmm.getTypeNameFromDbCardList(cafeteriaId, dateStr, date);
        if (!menus.isEmpty()) {
            card.setCardMenus(cafeteriaId, cafeteriaName, dateStr, date, menus);
            card.apply();
        }
    }
}