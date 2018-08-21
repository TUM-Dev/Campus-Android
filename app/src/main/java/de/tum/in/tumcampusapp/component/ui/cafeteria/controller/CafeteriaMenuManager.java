package de.tum.in.tumcampusapp.component.ui.cafeteria.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.tum.in.tumcampusapp.api.cafeteria.CafeteriaAPIClient;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.FavoriteDishDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.FavoriteFoodAlarmStorage;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaResponse;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.FavoriteDish;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Cafeteria Menu Manager, handles database stuff, external imports
 */
public class CafeteriaMenuManager {

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
    public void downloadMenus(boolean force) {
        // Responses from the cafeteria API are cached for one day. If the download is forced,
        // we add a "no-cache" header to the request.
        CacheControl cacheControl = force ? CacheControl.BYPASS_CACHE : CacheControl.USE_CACHE;

        CafeteriaAPIClient
                .getInstance(mContext)
                .getMenus(cacheControl)
                .enqueue(new Callback<CafeteriaResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CafeteriaResponse> call,
                                           @NonNull Response<CafeteriaResponse> response) {
                        CafeteriaResponse cafeteriaResponse = response.body();
                        if (cafeteriaResponse != null) {
                            onDownloadSuccess(cafeteriaResponse);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CafeteriaResponse> call, @NonNull Throwable t) {
                        Utils.log(t);
                    }
                });
    }

    private void onDownloadSuccess(@NonNull CafeteriaResponse response) {
        menuDao.removeCache();

        List<CafeteriaMenu> allMenus = response.getMenus();
        allMenus.addAll(response.getSideDishes());

        CafeteriaMenu[] menus = allMenus.toArray(new CafeteriaMenu[allMenus.size()]);
        menuDao.insert(menus);

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