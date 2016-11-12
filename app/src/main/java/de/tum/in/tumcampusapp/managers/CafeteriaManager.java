package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.CafeteriaMenuCard;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.models.cafeteria.Cafeteria;
import de.tum.in.tumcampusapp.models.cafeteria.CafeteriaMenu;

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
     * {"mensa":"4", "id":"411","name":"Mensa Leopoldstraße","anschrift"
     * :"Leopoldstraße 13a, München", "latitude":0.0000, "longitude":0.0000}
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
     * @throws JSONException
     */
    public void downloadFromExternal(boolean force) throws JSONException {
        SyncManager sync = new SyncManager(mContext);
        // Update table schemata if table exists
        if (!force && !sync.needSync(this, TIME_TO_SYNC)) {
            return;
        }

        Optional<JSONArray> jsonArray = new NetUtils(mContext).downloadJsonArray(CAFETERIAS_URL, CacheManager.VALIDITY_ONE_MONTH, false);
        if (!jsonArray.isPresent()) {
            return;
        }
        removeCache();

        // write cafeterias into database, transaction = speedup
        JSONArray arr = jsonArray.get();
        db.beginTransaction();
        try {
            for (int i = 0; i < arr.length(); i++) {
                replaceIntoDb(getFromJson(arr.getJSONObject(i)));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        sync.replaceIntoDb(this);
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
     * Returns a cafeteria by it's id
     *
     * @return Database cursor (id, name, address, latitude, longitude)
     */
    public Cursor getByIdFromDb(int id) {
        return db.query("cafeterias", null, null, null, "id = " + id, null, null);
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
    void replaceIntoDb(Cafeteria c) {
        if (c.id <= 0) {
            throw new IllegalArgumentException("Invalid id.");
        }
        if (c.name.isEmpty()) {
            throw new IllegalArgumentException("Invalid name.");
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
        // Choose which mensa should be shown
        int cafeteriaId = new LocationManager(context).getCafeteria();
        if (cafeteriaId == -1) {
            return;
        }

        // Get all available cafeterias from database
        Cursor cursor = getAllFromDb();
        String cafeteriaName = "";

        if (cursor.moveToFirst() && cursor.getColumnCount() >= 2) {
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
        CafeteriaMenuManager cmm = new CafeteriaMenuManager(context);
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
            CafeteriaMenuCard card = new CafeteriaMenuCard(context);
            card.setCardMenus(cafeteriaId, cafeteriaName, dateStr, date, menus);
            card.apply();
        }
    }

    /**
     * returns the menus of the best matching cafeteria
     */
    public Map<String, List<CafeteriaMenu>> getBestMatchMensaInfo(Context context) {
        // Choose which mensa should be shown
        int cafeteriaId = new LocationManager(context).getCafeteria();
        if (cafeteriaId == -1) {
            Utils.log("could not get a Cafeteria form locationManager!");
            return null;
        }

        // Get all available cafeterias from database
        Cursor cursor = getAllFromDb();
        String cafeteriaName = "";

        // get the cafeteria's name
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
        CafeteriaMenuManager cmm = new CafeteriaMenuManager(context);
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
        String mensaKey = cafeteriaName + ' ' + dateStr;
        Map<String, List<CafeteriaMenu>> selectedMensaMenus = new HashMap<>(1);
        selectedMensaMenus.put(mensaKey, menus);
        return selectedMensaMenus;
    }

    public String getBestMatchMensaName(Context context) {
        // Choose which mensa should be shown
        int cafeteriaId = new LocationManager(context).getCafeteria();
        if (cafeteriaId == -1) {
            Utils.log("could not get a Cafeteria form locationManager!");
            return null;
        }

        // Get all available cafeterias from database
        Cursor cursor = getAllFromDb();
        String cafeteriaName = "";

        // get the cafeteria's name
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
        CafeteriaMenuManager cmm = new CafeteriaMenuManager(context);
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
        }
        cursorCafeteriaDates.close();

        return cafeteriaName + ' ' + dateStr;
    }
}