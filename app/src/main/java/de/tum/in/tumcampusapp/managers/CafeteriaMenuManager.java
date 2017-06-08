package de.tum.in.tumcampusapp.managers;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import com.google.common.base.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.cafeteria.CafeteriaMenu;
import de.tum.in.tumcampusapp.services.FavoriteFoodAlarmEntry;

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
        notifyFavoriteFoodService(false);
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
        notifyFavoriteFoodService(true);
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
        notifyFavoriteFoodService(true);
    }
    /**
     * Queries all favorite dishes.
     * @return
     * A cursor to iterate through the dishnames.
     */
    private Cursor getAllFavoriteDishesByName(){
        return db.rawQuery("SELECT dishName FROM favorite_dishes",null);
    }

    /**
     * Prepares a bundle, which can be sent to the FavoriteDishAlarmScheduler, which contains all necessary
     * information to schedule the FavoriteDishAlarms. Its procedure is the following: Get the names
     * of all the favorite dishes and their corresponding mensaId (the user flags a food as favorite,
     * which also stores the mensaId). By assuming that the user will only rate the food as a favorite,
     * if he actually goes to that specific mensa, the method checks if there's any information about
     * any of these mensas serving a user's favorite food in the future. If that is the case a Bundle
     * containing the following information: MensaId, FavoriteDishName, Date will be constructed and
     * sent to the FavoriteDishAlarmScheduler. This way it is possible to schedule multiple alarms in advance.
     * @param resetAlarms
     * True if all currently scheduled alarms should be discarded, False if not
     */
    public void notifyFavoriteFoodService(boolean resetAlarms){
        if(resetAlarms){
            FavoriteFoodAlarmEntry.removeAll();
        }
        Cursor favoriteFoodWhere = db.rawQuery("SELECT mensaId,dishName FROM favorite_dishes GROUP BY mensaId,dishName",null);
        while (favoriteFoodWhere.moveToNext()){
            int mensaId = favoriteFoodWhere.getInt(0);
            String dishName = favoriteFoodWhere.getString(1);
            Cursor upcomingServings = db.rawQuery("SELECT mensaId,date,name FROM cafeterias_menus WHERE" +
                    " date >= date('now','localtime') AND mensaId = ? AND name = ?",
                    new String[]{""+mensaId, dishName});
            while (upcomingServings.moveToNext()){
                Calendar upcomingDate = Calendar.getInstance();
                upcomingDate.setTime(Utils.getDate(upcomingServings.getString(1)));
                new FavoriteFoodAlarmEntry(mensaId, dishName, upcomingDate, mContext);
            }
        }
    }

    /**
     * Constructs a cursor, which contains information about the dates a cafeteria serves a
     * specified dish.
     * @param dishName
     * The name of the dish
     * @return
     * A cursor to iterate through the results.
     */
    private Cursor getCafeteriasAndUpcomingServingDates(String dishName){
        return db.rawQuery("SELECT mensaId,date FROM cafeterias_menus WHERE date >= date('now','localtime') AND name = ?", new String[]{""+dishName});
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