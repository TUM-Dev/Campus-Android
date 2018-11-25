package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.tum.in.tumcampusapp.component.notifications.NotificationScheduler;
import de.tum.in.tumcampusapp.component.notifications.ProvidesNotifications;
import de.tum.in.tumcampusapp.component.notifications.model.AppNotification;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * News Manager, handles database stuff, external imports
 */
public class NewsController implements ProvidesNotifications {

    private final Context context;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    @Inject
    public NewsController(Context context) {
        this.context = context;
    }

    public void showNewsNotification(List<News> news, DateTime latestNewsDate) {
        if (!hasNotificationsEnabled()) {
            return;
        }

        List<News> newNews = new ArrayList<>();
        for (int i = 0; i < news.size(); i++) {
            News newsItem = news.get(i);
            if (newsItem.getDate().isAfter(latestNewsDate)) {
                newNews.add(newsItem);
            }
        }

        if (newNews.isEmpty()) {
            return;
        }

        NewsNotificationProvider provider = new NewsNotificationProvider(context, newNews);
        AppNotification notification = provider.buildNotification();

        if (notification != null) {
            NotificationScheduler scheduler = new NotificationScheduler(context);
            scheduler.schedule(notification);
        }
    }

    @Override
    public boolean hasNotificationsEnabled() {
        return Utils.getSettingBool(context, "card_news_phone", false);
    }

}