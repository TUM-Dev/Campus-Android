package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.tum.in.tumcampusapp.auxiliary.FavoriteFoodAlarmStorage;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.cafeteria.CafeteriaMenu;

/**
 * Cafeteria Menu Manager, handles database stuff, external imports
 */
public class CafeteriaMenuManager extends AbstractManager {

    private static final int TIME_TO_SYNC = 86400; // 1 day

    /**
     * Convert JSON object to CafeteriaMenu
     * Example JSON: e.g.
     * {"id":"25544","mensa_id":"411","date":"2011-06-20","type_short"
     * :"tg","type_long":"Tagesgericht 3","type_nr":"3","name":
     * "Cordon bleu vom Schwein (mit Formfleischhinterschinken) (S) (1,2,3,8)"}
     *
     * @param json see above
     * @return CafeteriaMenu
     * @throws JSONException if the json is invalid
     */
    private static CafeteriaMenu getFromJson(JSONObject json) throws JSONException {
        return new CafeteriaMenu(json.getInt("id"), json.getInt("mensa_id"),
                                 Utils.getDate(json.getString("date")),
                                 json.getString("type_short"), json.getString("type_long"),
                                 json.getInt("type_nr"), json.getString("name"));
    }

    /**
     * Convert JSON object to CafeteriaMenu (addendum)
     * <p/>
     * Example JSON: e.g.
     * {"mensa_id":"411","date":"2011-07-29","name":"Pflaumenkompott"
     * ,"type_short":"bei","type_long":"Beilagen"}
     *
     * @param json see above
     * @return CafeteriaMenu
     * @throws JSONException if the json is invalid
     */
    private static CafeteriaMenu getFromJsonAddendum(JSONObject json) throws JSONException {
        return new CafeteriaMenu(0, json.getInt("mensa_id"), Utils.getDate(json
                                                                                   .getString("date")), json.getString("type_short"),
                                 json.getString("type_long"), 10, json.getString("name"));
    }

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public CafeteriaMenuManager(Context context) {
        super(context);
        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS cafeterias_menus ("
                   + "id INTEGER PRIMARY KEY AUTOINCREMENT, mensaId INTEGER KEY, date VARCHAR, typeShort VARCHAR, "
                   + "typeLong VARCHAR, typeNr INTEGER, name VARCHAR)");

        db.execSQL("CREATE TABLE IF NOT EXISTS favorite_dishes ("
                   + "id INTEGER PRIMARY KEY AUTOINCREMENT, mensaId INTEGER, dishName VARCHAR,date VARCHAR, tag VARCHAR)");
    }

    /**
     * Download cafeteria menus from external interface (JSON)
     *
     * @param force True to force download over normal sync period, else false
     */
    public void downloadFromExternal(Context context, boolean force) {
        SyncManager sync = new SyncManager(mContext);
        if (!force && !sync.needSync(this, TIME_TO_SYNC)) {
            return;
        }
        String url = "http://lu32kap.typo3.lrz.de/mensaapp/exportDB.php?mensa_id=all";
        Optional<JSONObject> json = NetUtils.downloadJson(context, url);
        if (!json.isPresent()) {
            return;
        }
        JSONObject obj = json.get();
        db.beginTransaction();
        removeCache();
        try {
            JSONArray menu = obj.getJSONArray("mensa_menu");
            for (int j = 0; j < menu.length(); j++) {
                replaceIntoDb(getFromJson(menu.getJSONObject(j)));
            }
            JSONArray beilagen = obj.getJSONArray("mensa_beilagen");
            for (int j = 0; j < beilagen.length(); j++) {
                replaceIntoDb(getFromJsonAddendum(beilagen.getJSONObject(j)));
            }
            db.setTransactionSuccessful();
        } catch (JSONException e) {
            Utils.log(e);
        } finally {
            db.endTransaction();
        }
        sync.replaceIntoDb(this);
        scheduleFoodAlarms(true);
    }

    public void insertFavoriteDish(int mensaId, String dishName, String date, String tag) {
        db.execSQL("INSERT INTO favorite_dishes (mensaId, dishName, date, tag) VALUES (?, ?, ?,?)",
                   new String[]{String.valueOf(mensaId), dishName, date, tag});
        scheduleFoodAlarms(false);
    }

    public Cursor getFavoriteDishNextDates(int mensaId, String dishName) {
        return db.rawQuery("SELECT strftime('%d-%m-%Y', date) "
                           + "FROM cafeterias_menus WHERE date > date('now','localtime') AND mensaId=? AND name=?", new String[]{String.valueOf(mensaId), dishName});
    }

    public Cursor checkIfFavoriteDish(String tag) {
        return db.rawQuery("SELECT * "
                           + "FROM favorite_dishes WHERE tag=? ", new String[]{tag});
    }

    public Cursor getLastInsertedDishId(int mensaId, String dishName) {
        return db.rawQuery("SELECT MAX(id) "
                           + "FROM favorite_dishes WHERE mensaId=? AND dishName=?", new String[]{String.valueOf(mensaId), dishName});
    }

    public Cursor getFavoriteDishAllIds(int mensaId, String dishName) {
        return db.rawQuery("SELECT id "
                           + "FROM favorite_dishes WHERE mensaId=? AND dishName=?", new String[]{String.valueOf(mensaId), dishName});
    }

    public void deleteFavoriteDish(int mensaId, String dishName) {
        db.execSQL("DELETE "
                   + "FROM favorite_dishes WHERE mensaId=? AND dishName=?", new String[]{String.valueOf(mensaId), dishName});
        scheduleFoodAlarms(true);
    }

    public Cursor getFavoriteDishToday() {
        return db.rawQuery("SELECT dishName,mensaId FROM favorite_dishes WHERE date = date('now','localtime')", null);
    }

    /**
     * Get all distinct menu dates from the database
     *
     * @return Database cursor (date_de, _id)
     */
    public Cursor getDatesFromDb() {
        return db.rawQuery("SELECT DISTINCT strftime('%d.%m.%Y', date) as date_de, date as _id "
                           + "FROM cafeterias_menus WHERE date >= date('now','localtime') ORDER BY date", null);
    }

    /**
     * Get all types and names from the database for a special date and a special cafeteria
     *
     * @param mensaId Mensa ID, e.g. 411
     * @param date    ISO-Date, e.g. 2011-12-31
     * @return Database cursor (typeLong, names, _id, typeShort)
     */
    public Cursor getTypeNameFromDbCard(int mensaId, String date) {
        return db.rawQuery("SELECT typeLong, group_concat(name, '\n') as names, id as _id, typeShort "
                           + "FROM cafeterias_menus WHERE mensaId = ? AND "
                           + "date = ? GROUP BY typeLong ORDER BY typeShort=\"tg\" DESC, typeShort ASC, typeNr",
                           new String[]{String.valueOf(mensaId), date});
    }

    /**
     * Get all types and names from the database for a special date and a special cafeteria
     *
     * @param mensaId Mensa ID, e.g. 411
     * @param dateStr ISO-Date, e.g. 2011-12-31
     * @param date    Date
     * @return List of cafeteria menus
     */
    List<CafeteriaMenu> getTypeNameFromDbCardList(int mensaId, String dateStr, Date date) {
        List<CafeteriaMenu> menus = new ArrayList<>();
        try (Cursor cursorCafeteriaMenu = getTypeNameFromDbCard(mensaId, dateStr)) {
            if (cursorCafeteriaMenu.moveToFirst()) {
                do {
                    CafeteriaMenu menu = new CafeteriaMenu(Integer.parseInt(cursorCafeteriaMenu.getString(2)),
                                                           mensaId, date, cursorCafeteriaMenu.getString(3), cursorCafeteriaMenu.getString(0),
                                                           0, cursorCafeteriaMenu.getString(1));
                    menus.add(menu);
                } while (cursorCafeteriaMenu.moveToNext());
            }
        }
        return menus;
    }

    /**
     * Removes all cache items
     */
    private void removeCache() {
        db.execSQL("DELETE FROM cafeterias_menus");
    }

    /**
     * Replace or Insert a cafeteria menu in the database
     *
     * @param c CafeteriaMenu object
     */
    private void replaceIntoDb(CafeteriaMenu c) {
        db.execSQL("REPLACE INTO cafeterias_menus (mensaId, date, typeShort, "
                   + "typeLong, typeNr, name) VALUES (?, ?, ?, ?, ?, ?)",
                   new String[]{String.valueOf(c.cafeteriaId),
                                Utils.getDateString(c.date), c.typeShort, c.typeLong,
                                String.valueOf(c.typeNr), c.name});
    }

    /**
     * Prepares a bundle, which can be sent to the FavoriteDishAlarmScheduler, which contains all necessary
     * information to schedule the FavoriteDishAlarms. Its procedure is the following: Get the names
     * of all the favorite dishes and their corresponding mensaId (the user flags a food as favorite,
     * which also stores the mensaId). By assuming that the user will only rate the food as a favorite,
     * if he actually goes to that specific mensa. The alarm is then stored and scheduled, if it's not
     * scheduled already.
     *
     * @param completeReschedule True if all currently scheduled alarms should be discarded, False if not
     */
    public void scheduleFoodAlarms(boolean completeReschedule) {
        FavoriteFoodAlarmStorage favoriteFoodAlarmStorage = FavoriteFoodAlarmStorage.getInstance()
                                                                                    .initialize(mContext);
        if (completeReschedule) {
            favoriteFoodAlarmStorage.cancelOutstandingAlarms();
        }

        String query = "SELECT cafeterias_menus.date FROM favorite_dishes INNER JOIN cafeterias_menus" +
                       " ON cafeterias_menus.mensaId = favorite_dishes.mensaId" +
                       " AND favorite_dishes.dishName = cafeterias_menus.name";

        try (Cursor favoriteFoodWhere = db.rawQuery(query, null)) {
            while (favoriteFoodWhere.moveToNext()) {
                favoriteFoodAlarmStorage.scheduleAlarm(favoriteFoodWhere.getString(0));
            }
        }
    }

    /**
     * This method returns all the mensas serving favorite dishes at a given day and their unique
     * dishes
     *
     * @param dayMonthYear String with ISO-Date (yyyy-mm-dd)
     * @return the favourite dishes at the given date
     */
    public HashMap<Integer, HashSet<CafeteriaMenu>> getServedFavoritesAtDate(String dayMonthYear) {
        HashMap<Integer, HashSet<CafeteriaMenu>> cafeteriaServedDish = new HashMap<>();
        String query = "SELECT cafeterias_menus.* FROM favorite_dishes INNER JOIN cafeterias_menus" +
                       " ON cafeterias_menus.mensaId = favorite_dishes.mensaId" +
                       " AND favorite_dishes.dishName = cafeterias_menus.name WHERE cafeterias_menus.date = ?";

        try (Cursor upcomingServings = db.rawQuery(query, new String[]{dayMonthYear})) {
            while (upcomingServings.moveToNext()) {
                int mensaId = upcomingServings.getInt(upcomingServings.getColumnIndex("mensaId"));
                String dishName = upcomingServings.getString(upcomingServings.getColumnIndex("name"));

                int dishId = upcomingServings.getInt(upcomingServings.getColumnIndex("id"));
                String typeShort = upcomingServings.getString(upcomingServings.getColumnIndex("typeShort"));
                String typeLong = upcomingServings.getString(upcomingServings.getColumnIndex("typeLong"));
                int typeNr = upcomingServings.getInt(upcomingServings.getColumnIndex("typeNr"));
                CafeteriaMenu cafeteriaMenu = new CafeteriaMenu(dishId, mensaId, Utils.getDate(dayMonthYear), typeShort, typeLong, typeNr, dishName);
                HashSet<CafeteriaMenu> servedAtCafeteria;
                if (cafeteriaServedDish.containsKey(mensaId)) {
                    servedAtCafeteria = cafeteriaServedDish.get(mensaId);
                } else {
                    servedAtCafeteria = new HashSet<>();
                    cafeteriaServedDish.put(mensaId, servedAtCafeteria);
                }
                servedAtCafeteria.add(cafeteriaMenu);
            }
        }
        return cafeteriaServedDish;
    }
}