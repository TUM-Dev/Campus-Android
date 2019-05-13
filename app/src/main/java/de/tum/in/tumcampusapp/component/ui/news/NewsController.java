package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.notifications.NotificationScheduler;
import de.tum.in.tumcampusapp.component.notifications.ProvidesNotifications;
import de.tum.in.tumcampusapp.component.notifications.model.AppNotification;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.component.ui.tufilm.FilmCard;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Utils;
import de.tum.in.tumcampusapp.utils.sync.SyncManager;

import static de.tum.in.tumcampusapp.api.tumonline.CacheControl.USE_CACHE;

/**
 * News Manager, handles database stuff, external imports
 */
public class NewsController implements ProvidesCard, ProvidesNotifications {

    private static final int TIME_TO_SYNC = 86400;
    private final Context context;
    private final NewsDao newsDao;
    private final NewsSourcesDao newsSourcesDao;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    @Inject
    public NewsController(Context context) {
        this.context = context;
        newsDao = TcaDb.getInstance(context)
                       .newsDao();
        newsSourcesDao = TcaDb.getInstance(context)
                              .newsSourcesDao();
    }

    /**
     * Download news from external interface (JSON)
     *
     * @param force True to force download over normal sync period, else false
     */
    public void downloadFromExternal(CacheControl force) {
        SyncManager sync = new SyncManager(context);
        if (force == USE_CACHE && !sync.needSync(this, TIME_TO_SYNC)) {
            return;
        }

        News latestNews = newsDao.getLast();
        DateTime latestNewsDate = (latestNews != null) ? latestNews.getDate() : DateTime.now();

        // Delete all too old items
        newsDao.cleanUp();

        TUMCabeClient api = TUMCabeClient.getInstance(context);

        // Load all news sources
        try {
            List<NewsSources> sources = api.getNewsSources();
            if (sources != null) {
                newsSourcesDao.insert(sources);
            }
        } catch (IOException e) {
            Utils.log(e);
            return;
        }

        // Load all news since the last sync
        try {
            List<News> news = api.getNews(getLastId());
            if (news != null) {
                newsDao.insert(news);
            }
            showNewsNotification(news, latestNewsDate);
        } catch (IOException e) {
            Utils.log(e);
            return;
        }

        // Finish sync
        sync.replaceIntoDb(this);
    }

    private void showNewsNotification(List<News> news, DateTime latestNewsDate) {
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

    /**
     * Get all news from the database
     *
     * @return List of News
     */
    public List<News> getAllFromDb(Context context) {
        int selectedNewspread = Integer.parseInt(Utils.getSetting(this.context, "news_newspread", "7"));
        List<NewsSources> newsSources = getNewsSources();
        Collection<Integer> newsSourceIds = new ArrayList<>();
        for (NewsSources newsSource : newsSources) {
            int id = newsSource.getId();
            boolean show = Utils.getSettingBool(context, "news_source_" + id, id <= 7);
            if (show) {
                newsSourceIds.add(id);
            }
        }
        return newsDao.getAll(newsSourceIds.toArray(new Integer[newsSourceIds.size()]), selectedNewspread);
    }

    /**
     * Get the index of the newest item that is older than 'now'
     *
     * @return index of the newest item that is older than 'now' - 1
     */
    public int getTodayIndex() {
        int selectedNewspread = Integer.parseInt(Utils.getSetting(context, "news_newspread", "7"));
        List<News> news = newsDao.getNewer(selectedNewspread);
        return news.isEmpty() ? 0 : news.size() - 1;
    }

    private String getLastId() {
        News last = newsDao.getLast();
        return last == null ? "" : last.getId();
    }

    public List<NewsSources> getNewsSources() {
        String selectedNewspread = Utils.getSetting(context, "news_newspread", "7");
        return newsSourcesDao.getNewsSources(selectedNewspread);
    }

    public void setDismissed(String id, int d) {
        newsDao.setDismissed(String.valueOf(d), id);
    }

    /**
     * Gather all sources that should be displayed
     *
     * @param context
     * @return
     */
    private Collection<Integer> getActiveSources(Context context) {
        Collection<Integer> sources = new ArrayList<>();
        List<NewsSources> newsSources = getNewsSources();
        for (NewsSources newsSource : newsSources) {
            Integer id = newsSource.getId();
            if (Utils.getSettingBool(context, "card_news_source_" + id, true)) {
                sources.add(id);
            }
        }
        return sources;
    }

    @NotNull
    @Override
    public List<Card> getCards(@NonNull CacheControl cacheControl) {
        List<Card> results = new ArrayList<>();
        Collection<Integer> sources = getActiveSources(context);

        List<News> news;
        if (Utils.getSettingBool(context, "card_news_latest_only", true)) {
            news = newsDao.getBySourcesLatest(sources.toArray(new Integer[0]));
        } else {
            news = newsDao.getBySources(sources.toArray(new Integer[0]));
        }

        for (News n : news) {
            NewsCard card;
            if (n.isFilm()) {
                card = new FilmCard(context);
            } else {
                card = new NewsCard(context);
            }

            card.setNews(n);
            results.add(card.getIfShowOnStart());
        }

        return results;
    }

    @Override
    public boolean hasNotificationsEnabled() {
        return Utils.getSettingBool(context, "card_news_phone", false);
    }

}