package de.tum.in.tumcampusapp.component.ui.cafeteria.controller;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.tum.in.tumcampusapp.component.notifications.NotificationScheduler;
import de.tum.in.tumcampusapp.component.notifications.persistence.NotificationType;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationSettings;
import de.tum.in.tumcampusapp.component.ui.cafeteria.FavoriteDishDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;

/**
 * Cafeteria Menu Manager, handles database stuff, external imports
 */
public class CafeteriaMenuManager {

    private final Context mContext;
    private final CafeteriaMenuDao menuDao;
    private final FavoriteDishDao favoriteDishDao;
    private final CafeteriaNotificationSettings notificationSettings;
    private final NotificationScheduler notificationScheduler;

    @Inject
    public CafeteriaMenuManager(Context context, TcaDb database,
                                CafeteriaNotificationSettings settings,
                                NotificationScheduler scheduler) {
        mContext = context;
        menuDao = database.cafeteriaMenuDao();
        favoriteDishDao = database.favoriteDishDao();
        notificationSettings = settings;
        notificationScheduler = scheduler;
    }

    public void scheduleNotificationAlarms() {
        List<DateTime> menuDates = menuDao.getAllDates();
        List<DateTime> notificationTimes = new ArrayList<>();

        for (DateTime menuDate : menuDates) {
            LocalTime weekdayNotificationTime = notificationSettings.retrieveLocalTime(menuDate);
            if (weekdayNotificationTime != null) {
                DateTime notificationTime = weekdayNotificationTime.toDateTimeToday();
                notificationTimes.add(notificationTime);
            }
        }

        notificationScheduler.scheduleAlarms(NotificationType.CAFETERIA, notificationTimes);
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