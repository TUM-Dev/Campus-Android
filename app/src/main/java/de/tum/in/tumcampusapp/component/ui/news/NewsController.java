package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.component.ui.tufilm.FilmCard;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Utils;
import de.tum.in.tumcampusapp.utils.sync.SyncManager;

import static de.tum.in.tumcampusapp.utils.CacheManager.VALIDITY_ONE_DAY;

/**
 * News Manager, handles database stuff, external imports
 */
public class NewsController implements ProvidesCard {

    private static final int TIME_TO_SYNC = VALIDITY_ONE_DAY;
    private final Context context;
    private final NewsDao newsDao;
    private final NewsSourcesDao newsSourcesDao;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
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
     * @throws JSONException parsing could fail
     */
    public void downloadFromExternal(boolean force) throws JSONException {
        SyncManager sync = new SyncManager(context);
        if (!force && !sync.needSync(this, TIME_TO_SYNC)) {
            return;
        }

        // Delete all too old items
        newsDao.cleanUp();

        TUMCabeClient api = TUMCabeClient.getInstance(context);

        // Load all news sources
        try {
            List sources = api.getNewsSources();
            newsSourcesDao.insert(sources);
        } catch (IOException e) {
            Utils.log(e);
            return;
        }


        // Load all news since the last sync
        try {
            List news = api.getNews(getLastId());
            newsDao.insert(news);
        } catch (IOException e) {
            Utils.log(e);
            return;
        }

        //Finish sync
        sync.replaceIntoDb(this);
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

    /**
     * Adds the newest news card
     *
     * @param context Context
     */
    @Override
    public void onRequestCard(@NonNull Context context) {
        Collection<Integer> sources = getActiveSources(context);

        List<News> news;
        if (Utils.getSettingBool(context, "card_news_latest_only", true)) {
            news = newsDao.getBySourcesLatest(sources.toArray(new Integer[sources.size()]));
        } else {
            news = newsDao.getBySources(sources.toArray(new Integer[sources.size()]));
        }

        //Display resulting cards
        for (News n : news) {
            NewsCard card;
            if (n.isFilm()) {
                card = new FilmCard(context);
            } else {
                card = new NewsCard(context);
            }
            card.setNews(n);
            card.apply();
        }
    }
}