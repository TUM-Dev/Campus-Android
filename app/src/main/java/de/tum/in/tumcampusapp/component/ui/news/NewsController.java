package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.tufilm.FilmCard;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.CacheManager;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateUtils;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import de.tum.in.tumcampusapp.utils.sync.SyncManager;

/**
 * News Manager, handles database stuff, external imports
 */
public class NewsController implements Card.ProvidesCard {

    private static final int TIME_TO_SYNC = 1800; // 1/2 hour
    private static final String NEWS_URL = "https://tumcabe.in.tum.de/Api/news/";
    private static final String NEWS_SOURCES_URL = NEWS_URL + "sources";
    private final Context mContext;
    private final NewsDao newsDao;
    private final NewsSourcesDao newsSourcesDao;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public NewsController(Context context) {
        mContext = context;
        newsDao = TcaDb.getInstance(context)
                       .newsDao();
        newsSourcesDao = TcaDb.getInstance(context)
                              .newsSourcesDao();
    }

    /**
     * Removes all old items (older than 3 months)
     */
    private void cleanupDb() {
        newsDao.cleanUp();
    }

    /**
     * Download news from external interface (JSON)
     *
     * @param force True to force download over normal sync period, else false
     * @throws JSONException
     */
    public void downloadFromExternal(boolean force) throws JSONException {
        SyncManager sync = new SyncManager(mContext);
        if (!force && !sync.needSync(this, TIME_TO_SYNC)) {
            return;
        }

        NetUtils net = new NetUtils(mContext);
        // Load all news sources
        Optional<JSONArray> jsonArray = net.downloadJsonArray(NEWS_SOURCES_URL, CacheManager.VALIDITY_ONE_MONTH, force);

        if (jsonArray.isPresent()) {
            JSONArray arr = jsonArray.get();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                newsSourcesDao.insert(new NewsSources(obj.getInt(Const.JSON_SOURCE),
                                                      obj.getString(Const.JSON_TITLE),
                                                      obj.has(Const.JSON_ICON) ? obj.getString(Const.JSON_ICON) : ""));
            }
        }

        // Load all news since the last sync
        jsonArray = net.downloadJsonArray(NEWS_URL + getLastId(), CacheManager.VALIDITY_ONE_DAY, force);

        // Delete all too old items
        cleanupDb();

        if (!jsonArray.isPresent()) {
            return;
        }

        JSONArray arr = jsonArray.get();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            newsDao.insert(new News(obj.getString(Const.JSON_NEWS),
                                    obj.getString(Const.JSON_TITLE),
                                    obj.getString(Const.JSON_LINK),
                                    obj.getString(Const.JSON_SRC),
                                    obj.getString(Const.JSON_IMAGE),
                                    DateUtils.getDateTime(obj.getString(Const.JSON_DATE)),
                                    DateUtils.getDateTime(obj.getString(Const.JSON_CREATED)),
                                    0));
        }

        sync.replaceIntoDb(this);
    }

    /**
     * Get all news from the database
     *
     * @return List of News
     */
    public List<News> getAllFromDb(Context context) {
        int selectedNewspread = Integer.parseInt(Utils.getSetting(mContext, "news_newspread", "7"));
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
        int selectedNewspread = Integer.parseInt(Utils.getSetting(mContext, "news_newspread", "7"));
        List<News> news = newsDao.getNewer(selectedNewspread);
        return news.size() == 0 ? 0 : news.size() - 1;
    }

    private String getLastId() {
        News last = newsDao.getLast();
        return last == null ? "" : last.getId();
    }

    public List<NewsSources> getNewsSources() {
        String selectedNewspread = Utils.getSetting(mContext, "news_newspread", "7");
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
    public void onRequestCard(Context context) {
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