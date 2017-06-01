package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.cafeteria.CafeteriaMenu;
import de.tum.in.tumcampusapp.services.FavoriteDishAlarm;
import de.tum.in.tumcampusapp.services.FavoriteDishService;

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
     * @throws JSONException
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
     * @throws JSONException
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
    }

    public void insertFavoriteDish(int mensaId, String dishName, String date, String tag) {
        db.execSQL("INSERT INTO favorite_dishes (mensaId, dishName, date, tag) VALUES (?, ?, ?,?)",
                new String[]{String.valueOf(mensaId), dishName, date, tag});
        notifyFavoriteFoodService();
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
    public List<CafeteriaMenu> getTypeNameFromDbCardList(int mensaId, String dateStr, Date date) {
        Cursor cursorCafeteriaMenu = getTypeNameFromDbCard(mensaId, dateStr);
        ArrayList<CafeteriaMenu> menus = new ArrayList<>();
        if (cursorCafeteriaMenu.moveToFirst()) {
            do {
                CafeteriaMenu menu = new CafeteriaMenu(Integer.parseInt(cursorCafeteriaMenu.getString(2)),
                        mensaId, date, cursorCafeteriaMenu.getString(3), cursorCafeteriaMenu.getString(0),
                        0, cursorCafeteriaMenu.getString(1));
                menus.add(menu);
            } while (cursorCafeteriaMenu.moveToNext());
        }
        cursorCafeteriaMenu.close();
        return menus;
    }

    /**
     * Removes all cache items
     */
    public void removeCache() {
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
        notifyFavoriteFoodService();
    }

    /**
     * This method has to be called after we updated the local favorite database or if the cafeterias'
     * menus have changed. It first checks whether there are any cafeterias, which serve a favorite food
     * and only if there's at least one we query the location manager to see if it is the prefered one.
     * Then a background service is started, which sends a notification at a given time and makes sure,
     * that there's only one of these notifications per day.
     */
    public void notifyFavoriteFoodService(){
        Cursor c = db.rawQuery("SELECT dishName, mensaId FROM favorite_dishes", null);
        String dishName;
        int mensaId;

        HashMap<Integer, ArrayList<String>> mensaServesDish = new HashMap<>();
        while(c.moveToNext()){
            dishName = c.getString(c.getColumnIndex("dishName"));
            mensaId = c.getInt(c.getColumnIndex("mensaId"));
            if (mensaServesDishToday(mensaId, dishName)) {
                if (mensaServesDish.containsKey(mensaId)){
                    mensaServesDish.get(mensaId).add(dishName);
                }else{
                    ArrayList<String> dishNames = new ArrayList<>();
                    dishNames.add(dishName);
                    mensaServesDish.put(mensaId, dishNames);
                }
            }
        }
        if (mensaServesDish.size() > 0){
            LocationManager lm = new LocationManager(mContext);
            int bestMensaId = lm.getCafeteria();
            Bundle notificationData = new Bundle();
            if (mensaServesDish.containsKey(bestMensaId)){
                notificationData.putInt("bestMensa",bestMensaId);
            }
            ArrayList<Integer> mensaIds = new ArrayList<>();
            mensaIds.addAll(mensaServesDish.keySet());
            notificationData.putIntegerArrayList("mensaIds", mensaIds);
            for (Integer key : mensaServesDish.keySet()){
                notificationData.putStringArrayList(""+key,mensaServesDish.get(key));
            }
            new FavoriteDishAlarm(mContext, notificationData, 1,00);
        }
    }

    /**
     *
     * @param mensaId   The id of a cafeteria
     * @param dishName  The name of the dish
     * @return True if the cafeteria serves the given dish, false otherwise
     */
    private boolean mensaServesDishToday(int mensaId, String dishName){
        Cursor isServedToday = db.rawQuery("SELECT name,mensaId FROM cafeterias_menus WHERE date = date('now','localtime') AND mensaId = ? AND name = ?", new String[]{""+mensaId, dishName});
        return (isServedToday.getCount() > 0);
    }
}