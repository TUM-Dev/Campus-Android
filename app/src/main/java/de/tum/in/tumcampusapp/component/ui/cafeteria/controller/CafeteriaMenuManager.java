package de.tum.in.tumcampusapp.component.ui.cafeteria.controller;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
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
    @Inject
    public CafeteriaMenuManager(Context context) {
        mContext = context;
        TcaDb db = TcaDb.getInstance(context);
        menuDao = db.cafeteriaMenuDao();
        favoriteDishDao = db.favoriteDishDao();
    }

    /**
     * Download cafeteria menus from external interface (JSON)
     *
     * @param cacheControl BYPASS_CACHE to force download over normal sync period, else false
     */
    public void downloadMenus(CacheControl cacheControl) {
        // Responses from the cafeteria API are cached for one day. If the download is forced,
        // we add a "no-cache" header to the request.
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
     * Returns all the favorite dishes that a particular mensa serves on the specified date.
     *
     * @param queriedMensaId The Cafeteria for which to return the favorite dishes served
     * @param date The date for which to return the favorite dishes served
     * @return the favourite dishes at the given date
     */
    public List<CafeteriaMenu> getFavoriteDishesServed(int queriedMensaId, DateTime date) {
        List<CafeteriaMenu> results = new ArrayList<>();
        String dateString = DateTimeUtils.INSTANCE.getDateString(date);

        List<CafeteriaMenu> upcomingServings = favoriteDishDao.getFavouritedCafeteriaMenuOnDate(dateString);

        for (CafeteriaMenu upcomingServing : upcomingServings) {
            int currentMensaId = upcomingServing.getCafeteriaId();

            if (currentMensaId == queriedMensaId) {
                results.add(upcomingServing);
            }
        }

        return results;
    }

}