package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import de.tum.in.tumcampus.activities.CurriculaActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.CalendarRowSet;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;
import de.tum.in.tumcampus.models.OrgItemList;
import de.tum.in.tumcampus.models.TuitionList;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;

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

    public static final Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    public static final LruCache<String, Bitmap> bitmapCache;

    static {
        int cacheSize = 4 * 1024 * 1024; // 4MiB
        bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();

            }
        };
    }

    /**
     * Database connection
     */
    private final SQLiteDatabase db;
    private final Context mContext;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public CacheManager(Context context) {
        mContext = context;
        db = DatabaseManager.getDb(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS cache (url VARCHAR UNIQUE, data BLOB, " +
                "validity VARCHAR, max_age VARCHAR, typ INTEGER)");

        // Delete all entries that are too old and delete corresponding image files
        db.beginTransaction();
        Cursor cur = db.rawQuery("SELECT data FROM cache WHERE datetime()>max_age AND typ=1", null);
        if (cur.moveToFirst()) {
            do {
                File f = new File(cur.getString(0));
                f.delete();
            } while (cur.moveToNext());
        }
        cur.close();
        db.execSQL("DELETE FROM cache WHERE datetime()>max_age");
        db.setTransactionSuccessful();
        db.endTransaction();
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
        Cursor cur = news.getNewsSources();
        if (cur.moveToFirst()) {
            do {
                String imgUrl = cur.getString(1);
                if (!imgUrl.isEmpty() && !imgUrl.equals("null"))
                    net.downloadImage(imgUrl);
            } while (cur.moveToNext());
        }
        cur.close();

        // Cache news images
        cur = news.getAllFromDb(mContext);
        if (cur.moveToFirst()) {
            do {
                String imgUrl = cur.getString(4);
                if (!imgUrl.equals("null"))
                    net.downloadImage(imgUrl);
            } while (cur.moveToNext());
        }
        cur.close();

        // Cache kino covers
        KinoManager km = new KinoManager(mContext);
        cur = km.getAllFromDb();
        if (cur.moveToFirst()) {
            do {
                String imgUrl = cur.getString(cur.getColumnIndex(Const.JSON_COVER));
                if (!imgUrl.equals("null"))
                    net.downloadImage(imgUrl);
            } while (cur.moveToNext());
        }
        cur.close();

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
            CalendarRowSet set = requestHandler.fetch();
            if (set != null) {
                CalendarManager calendarManager = new CalendarManager(mContext);
                calendarManager.importCalendar(set);
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
    public String getFromCache(String url) {
        String result = null;

        try {
            Cursor c = db.rawQuery("SELECT data FROM cache WHERE url=? AND datetime()<max_age", new String[]{url});
            if (c.getCount() == 1) {
                c.moveToFirst();
                result = c.getString(0);
            }
            c.close();
        } catch (SQLiteException e) {
            Utils.log(e);
        }
        return result;
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date
     *
     * @param url Url from which data was cached
     * @return Data if valid version was found, null if no data is available
     */
    public boolean shouldRefresh(String url) {
        boolean result = true;

        try {
            Cursor c = db.rawQuery("SELECT url FROM cache WHERE url=? AND datetime() < validity", new String[]{url});
            if (c.getCount() == 1) {
                result = false;
            }
            c.close();
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
        if (validity == VALIDITY_DO_NOT_CACHE)
            return;
        Utils.logv("replace " + url + " " + data);
        db.execSQL("REPLACE INTO cache (url, data, validity, max_age, typ) " +
                        "VALUES (?, ?, datetime('now','+" + (validity / 2) + " seconds'), " +
                        "datetime('now','+" + validity + " seconds'), ?)",
                new String[]{url, data, "" + typ});
    }

    /**
     * this function allows us to import all lecture items from TUMOnline
     */
    void importLecturesFromTUMOnline() {
        // get my lectures
        TUMOnlineRequest<LecturesSearchRowSet> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.LECTURES_PERSONAL, mContext);
        if (!shouldRefresh(requestHandler.getRequestURL()))
            return;

        LecturesSearchRowSet lecturesList = requestHandler.fetch();
        if (lecturesList == null)
            return;
        List<LecturesSearchRow> lectures = lecturesList.getLehrveranstaltungen();
        if (lectures == null)
            return;
        ChatRoomManager manager = new ChatRoomManager(mContext);
        manager.replaceInto(lectures);
    /*    if (myLecturesList == null)
            return;

        // get schedule for my lectures
        for (int i = 0; i < myLecturesList.getLehrveranstaltungen().size(); i++) {
            LecturesSearchRow currentLecture = myLecturesList.getLehrveranstaltungen().get(i);

            // now, get appointments for each lecture
            TUMOnlineRequest<LectureAppointmentsRowSet> req = new TUMOnlineRequest<LectureAppointmentsRowSet>(TUMOnlineConst.LECTURES_APPOINTMENTS, mContext);
            req.setParameter("pLVNr", currentLecture.getStp_sp_nr());
            req.fetch();

            TUMOnlineRequest<LectureDetailsRowSet> req2 = new TUMOnlineRequest<LectureDetailsRowSet>(TUMOnlineConst.LECTURES_DETAILS, mContext);
            req2.setParameter("pLVNr", currentLecture.getStp_sp_nr());
            req2.fetch();
        }*/
    }

    public void clearCache() {
        // Delete all entries that are too old and delete corresponding image files
        Cursor cur = db.rawQuery("SELECT data FROM cache WHERE typ=1", null);
        if (cur.moveToFirst()) {
            do {
                File f = new File(cur.getString(0));
                f.delete();
            } while (cur.moveToNext());
        }
        cur.close();
    }
}