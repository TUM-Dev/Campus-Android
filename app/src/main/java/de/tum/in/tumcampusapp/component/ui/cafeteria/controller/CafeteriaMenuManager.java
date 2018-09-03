package de.tum.in.tumcampusapp.component.ui.cafeteria.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tum.in.tumcampusapp.api.cafeteria.CafeteriaAPIClient;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.notifications.NotificationScheduler;
import de.tum.in.tumcampusapp.component.notifications.persistence.NotificationType;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationSettings;
import de.tum.in.tumcampusapp.component.ui.cafeteria.FavoriteDishDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaResponse;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
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

        scheduleNotificationAlarms();
    }

    public void scheduleNotificationAlarms() {
        List<DateTime> menuDates = menuDao.getAllDates();
        List<DateTime> notificationTimes = new ArrayList<>();

        CafeteriaNotificationSettings settings = new CafeteriaNotificationSettings(mContext);

        for (DateTime menuDate : menuDates) {
            LocalTime weekdayNotificationTime = settings.retrieveLocalTime(menuDate);
            if (weekdayNotificationTime != null) {
                DateTime notificationTime = weekdayNotificationTime.toDateTimeToday();
                notificationTimes.add(notificationTime);
            }
        }

        NotificationScheduler scheduler = new NotificationScheduler(mContext);
        scheduler.scheduleAlarms(NotificationType.CAFETERIA, notificationTimes);
    }

    /**
     * This method returns all the mensas serving favorite dishes at a given day and their unique
     * dishes
     *
     * @param date DateTime with ISO-Date (yyyy-mm-dd)
     * @return the favourite dishes at the given date
     */
    public ArrayMap<Integer, HashSet<CafeteriaMenu>> getFavoritesServedAtDate(DateTime date) {
        ArrayMap<Integer, HashSet<CafeteriaMenu>> results = new ArrayMap<>();
        String dateString = DateTimeUtils.INSTANCE.getDateString(date);
        List<CafeteriaMenu> upcomingServings = favoriteDishDao.getFavouritedCafeteriaMenuOnDate(dateString);

        for (CafeteriaMenu upcomingServing : upcomingServings) {
            int mensaId = upcomingServing.getCafeteriaId();
            HashSet<CafeteriaMenu> servedAtCafeteria;

            if (results.containsKey(mensaId)) {
                servedAtCafeteria = results.get(mensaId);
            } else {
                servedAtCafeteria = new HashSet<>();
                results.put(mensaId, servedAtCafeteria);
            }

            servedAtCafeteria.add(upcomingServing);
        }

        return results;
    }

    public Set<CafeteriaMenu> getFavoriteDishesServed(int mensaId, DateTime date) {
        Set<CafeteriaMenu> results = new HashSet<>();
        String dateString = DateTimeUtils.INSTANCE.getDateString(date);
        List<CafeteriaMenu> upcomingServings = favoriteDishDao.getFavouritedCafeteriaMenuOnDate(dateString);

        for (CafeteriaMenu upcomingServing : upcomingServings) {
            int cafeteriaId = upcomingServing.getCafeteriaId();

            if (cafeteriaId == mensaId) {
                results.add(upcomingServing);
            }
        }

        return results;
    }

}