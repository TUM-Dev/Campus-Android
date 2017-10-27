package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.google.common.base.Optional;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import de.tum.in.tumcampusapp.activities.CurriculaActivity;
import de.tum.in.tumcampusapp.auxiliary.AccessTokenManager;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumo.CalendarRowSet;
import de.tum.in.tumcampusapp.models.tumo.LecturesSearchRow;
import de.tum.in.tumcampusapp.models.tumo.LecturesSearchRowSet;
import de.tum.in.tumcampusapp.models.tumo.OrgItemList;
import de.tum.in.tumcampusapp.models.tumo.TuitionList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class CacheManager {
    public static final int CACHE_TYP_DATA = 0;
    public static final int CACHE_TYP_IMAGE = 1;

    /**
     * Validity's for entries in seconds
     */
    public static final int VALIDITY_DO_NOT_CACHE = 0;
    public static final int VALIDITY_ONE_DAY = 86400;
    public static final int VALIDITY_TWO_DAYS = 2 * 86400;
    public static final int VALIDITY_FIFE_DAYS = 5 * 86400;
    public static final int VALIDITY_TEN_DAYS = 10 * 86400;
    public static final int VALIDITY_ONE_MONTH = 30 * 86400;

    public static final Map<ImageView, String> IMAGE_VIEWS = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    public static final LruCache<String, Bitmap> BITMAP_CACHE;

    private static SQLiteDatabase cacheDb;
    private final Context mContext;

    static {
        int cacheSize = 4 * 1024 * 1024; // 4MiB
        BITMAP_CACHE = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();

            }
        };
    }

    private static synchronized void initCacheDb(Context c) {
        if (cacheDb == null) {
            File dbFile = new File(c.getCacheDir()
                                    .getAbsolutePath() + "/cache.db");
            dbFile.getParentFile()
                  .mkdirs();
            cacheDb = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        }
    }

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public CacheManager(Context context) {
        initCacheDb(context);
        mContext = context;

        // create table if needed
        cacheDb.execSQL("CREATE TABLE IF NOT EXISTS cache (url VARCHAR UNIQUE, data BLOB, " +
                        "validity VARCHAR, max_age VARCHAR, typ INTEGER)");

        // Delete all entries that are too old and delete corresponding image files
        cacheDb.beginTransaction();
        try (Cursor cur = cacheDb.rawQuery("SELECT data FROM cache WHERE datetime()>max_age AND typ=1", null)) {
            if (cur.moveToFirst()) {
                do {
                    File f = new File(cur.getString(0));
                    f.delete();
                } while (cur.moveToNext());
            }
        }
        cacheDb.execSQL("DELETE FROM cache WHERE datetime()>max_age");
        cacheDb.setTransactionSuccessful();
        cacheDb.endTransaction();
    }

    /**
     * Download usual tumOnline requests
     */
    public void fillCache() {

        NetUtils net = new NetUtils(mContext);
        // Cache curricula urls
        if (shouldRefresh(CurriculaActivity.CURRICULA_URL)) {
            net.downloadJsonArray(CurriculaActivity.CURRICULA_URL, CacheManager.VALIDITY_ONE_MONTH, true);
        }

        // Cache news source images
        NewsManager news = new NewsManager(mContext);
        try (Cursor cur = news.getNewsSources()) {
            if (cur.moveToFirst()) {
                do {
                    String imgUrl = cur.getString(1);
                    if (!imgUrl.isEmpty() && !"null".equals(imgUrl)) {
                        net.downloadImage(imgUrl);
                    }
                } while (cur.moveToNext());
            }
        }

        // Cache news images
        try (Cursor cur = news.getAllFromDb(mContext)) {
            if (cur.moveToFirst()) {
                do {
                    String imgUrl = cur.getString(4);
                    if (!"null".equals(imgUrl)) {
                        net.downloadImage(imgUrl);
                    }
                } while (cur.moveToNext());
            }
        }

        // Cache kino covers
        KinoManager km = new KinoManager(mContext);
        try (Cursor cur = km.getAllFromDb()) {
            if (cur.moveToFirst()) {
                do {
                    String imgUrl = cur.getString(cur.getColumnIndex(Const.JSON_COVER));
                    if (!"null".equals(imgUrl)) {
                        net.downloadImage(imgUrl);
                    }
                } while (cur.moveToNext());
            }
        }

        // acquire access token
        if (!new AccessTokenManager(mContext).hasValidAccessToken()) {
            return;
        }

        // ALL STUFF BELOW HERE NEEDS A VALID ACCESS TOKEN

        // Sync organisation tree
        TUMOnlineRequest<OrgItemList> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.ORG_TREE, mContext);
        if (shouldRefresh(requestHandler.getRequestURL())) {
            requestHandler.fetch();
        }

        // Sync fee status
        TUMOnlineRequest<TuitionList> requestHandler2 = new TUMOnlineRequest<>(TUMOnlineConst.TUITION_FEE_STATUS, mContext);
        if (shouldRefresh(requestHandler2.getRequestURL())) {
            requestHandler2.fetch();
        }

        // Sync lectures, details and appointments
        importLecturesFromTUMOnline();

        // Sync calendar
        syncCalendar();
    }

    public void syncCalendar() {
        TUMOnlineRequest<CalendarRowSet> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.CALENDER, mContext);
        requestHandler.setParameter("pMonateVor", "0");
        requestHandler.setParameter("pMonateNach", "3");
        if (shouldRefresh(requestHandler.getRequestURL())) {
            Optional<CalendarRowSet> set = requestHandler.fetch();
            if (set.isPresent()) {
                CalendarManager calendarManager = new CalendarManager(mContext);
                calendarManager.importCalendar(set.get());
                CalendarManager.QueryLocationsService.loadGeo(mContext);
            }
        }
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date and returns the cache content
     * if data is up to date
     *
     * @param url Url from which data was cached
     * @return Data if valid version was found, null if no data is available
     */
    public Optional<String> getFromCache(String url) {
        String result = null;

        try (Cursor c = cacheDb.rawQuery("SELECT data FROM cache WHERE url=? AND datetime()<max_age", new String[]{url})) {
            if (c.getCount() == 1) {
                c.moveToFirst();
                result = c.getString(0);
            }
        } catch (SQLiteException e) {
            Utils.log(e);
        }
        return Optional.fromNullable(result);
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date
     *
     * @param url Url from which data was cached
     * @return Data if valid version was found, null if no data is available
     */
    private boolean shouldRefresh(String url) {
        boolean result = true;

        try (Cursor c = cacheDb.rawQuery("SELECT url FROM cache WHERE url=? AND datetime() < validity", new String[]{url})) {
            if (c.getCount() == 1) {
                result = false;
            }
        } catch (SQLiteException e) {
            Utils.log(e);
        }
        return result;
    }

    /**
     * Add a result to cache
     *
     * @param url  url from where the data was fetched
     * @param data result
     */
    public void addToCache(String url, String data, int validity, int typ) {
        if (validity == VALIDITY_DO_NOT_CACHE) {
            return;
        }
        cacheDb.execSQL("REPLACE INTO cache (url, data, validity, max_age, typ) " +
                        "VALUES (?, ?, datetime('now','+" + (validity / 2) + " seconds'), " +
                        "datetime('now','+" + validity + " seconds'), ?)",
                        new String[]{url, data, String.valueOf(typ)});
    }

    /**
     * this function allows us to import all lecture items from TUMOnline
     */
    private void importLecturesFromTUMOnline() {
        // get my lectures
        TUMOnlineRequest<LecturesSearchRowSet> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.LECTURES_PERSONAL, mContext);
        if (!shouldRefresh(requestHandler.getRequestURL())) {
            return;
        }

        Optional<LecturesSearchRowSet> lecturesList = requestHandler.fetch();
        if (!lecturesList.isPresent()) {
            return;
        }
        List<LecturesSearchRow> lectures = lecturesList.get()
                                                       .getLehrveranstaltungen();
        if (lectures == null) {
            return;
        }
        ChatRoomManager manager = new ChatRoomManager(mContext);
        manager.replaceInto(lectures);
    }

    static synchronized void clearCache(Context context) {
        cacheDb = null;
        try {
            Process proc = Runtime.getRuntime()
                                  .exec("rm -r " + context.getCacheDir());
            proc.waitFor();
        } catch (InterruptedException | IOException e) {
            Utils.log("couldn't delete cache files " + e.toString());
        }
    }
}