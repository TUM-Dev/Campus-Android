package de.tum.in.tumcampusapp.component.ui.cafeteria.controller;

import android.content.Context;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.FavoriteDishDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.FavoriteFoodAlarmStorage;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.FavoriteDish;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import de.tum.in.tumcampusapp.utils.sync.SyncManager;

/**
 * Cafeteria Menu Manager, handles database stuff, external imports
 */
public class CafeteriaMenuManager {

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
                DateTimeUtils.INSTANCE.getDate(json.getString("date")),
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
        return new CafeteriaMenu(0, json.getInt("mensa_id"),
                DateTimeUtils.INSTANCE.getDate(json.getString("date")),
                json.getString("type_short"), json.getString("type_long"),
                10, json.getString("name"));
    }

    private final Context mContext;
    private final CafeteriaMenuDao menuDao;
    private final FavoriteDishDao favoriteDishDao;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public CafeteriaMenuManager(Context context) {
        mContext = context;
        TcaDb db = TcaDb.getInstance(context);
        menuDao = db.cafeteriaMenuDao();
        favoriteDishDao = db.favoriteDishDao();
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
        menuDao.removeCache();
        try {
            JSONArray menu = obj.getJSONArray("mensa_menu");
            for (int j = 0; j < menu.length(); j++) {
                menuDao.insert(getFromJson(menu.getJSONObject(j)));
            }
            JSONArray beilagen = obj.getJSONArray("mensa_beilagen");
            for (int j = 0; j < beilagen.length(); j++) {
                menuDao.insert(getFromJsonAddendum(beilagen.getJSONObject(j)));
            }
        } catch (JSONException e) {
            Utils.log(e);
        }
        sync.replaceIntoDb(this);
        scheduleFoodAlarms(true);
    }

    public void insertFavoriteDish(int mensaId, String dishName, String date, String tag) {
        favoriteDishDao.insertFavouriteDish(FavoriteDish.Companion.create(mensaId, dishName, date, tag));
        scheduleFoodAlarms(false);
    }

    public void deleteFavoriteDish(int mensaId, String dishName) {
        favoriteDishDao.deleteFavoriteDish(mensaId, dishName);
        scheduleFoodAlarms(true);
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

        List<String> dates = favoriteDishDao.getFavouriteDishDates();

        for (String date : dates) {
            favoriteFoodAlarmStorage.scheduleAlarm(date);
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

        List<CafeteriaMenu> upcomingServings = favoriteDishDao.getFavouritedCafeteriaMenuOnDate(dayMonthYear);
        for (CafeteriaMenu upcomingServing : upcomingServings) {
            int mensaId = upcomingServing.getCafeteriaId();
            HashSet<CafeteriaMenu> servedAtCafeteria;
            if (cafeteriaServedDish.containsKey(mensaId)) {
                servedAtCafeteria = cafeteriaServedDish.get(mensaId);
            } else {
                servedAtCafeteria = new HashSet<>();
                cafeteriaServedDish.put(mensaId, servedAtCafeteria);
            }
            servedAtCafeteria.add(upcomingServing);
        }

        return cafeteriaServedDish;
    }
}