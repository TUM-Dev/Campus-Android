package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.google.common.base.Optional;

import org.joda.time.DateTime;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import de.tum.in.tumcampusapp.activities.CurriculaActivity;
import de.tum.in.tumcampusapp.auxiliary.AccessTokenManager;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.entities.CacheItem;
import de.tum.in.tumcampusapp.entities.CacheItem_;
import de.tum.in.tumcampusapp.entities.MyBoxStore;
import de.tum.in.tumcampusapp.models.tumo.CalendarRowSet;
import de.tum.in.tumcampusapp.models.tumo.LecturesSearchRow;
import de.tum.in.tumcampusapp.models.tumo.LecturesSearchRowSet;
import de.tum.in.tumcampusapp.models.tumo.OrgItemList;
import de.tum.in.tumcampusapp.models.tumo.TuitionList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import io.objectbox.Box;


/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class CacheManager extends AbstractManager {
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
    private Box<CacheItem> cacheBox;

    static {
        int cacheSize = 4 * 1024 * 1024; // 4MiB
        BITMAP_CACHE = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();

            }
        };
    }

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public CacheManager(Context context) {
        super(context);

        // create table if needed
        cacheBox = MyBoxStore.getBoxStore().boxFor(CacheItem.class);

        // Delete all entries that are too old and delete corresponding image files
        this.clearCache(true);
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
                if (!imgUrl.isEmpty() && !"null".equals(imgUrl)) {
                    net.downloadImage(imgUrl);
                }
            } while (cur.moveToNext());
        }
        cur.close();

        // Cache news images
        cur = news.getAllFromDb(mContext);
        if (cur.moveToFirst()) {
            do {
                String imgUrl = cur.getString(4);
                if (!"null".equals(imgUrl)) {
                    net.downloadImage(imgUrl);
                }
            } while (cur.moveToNext());
        }
        cur.close();

        // Cache kino covers
        KinoManager km = new KinoManager(mContext);
        km.cacheCovers();

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
        TUMOnlineRequest<LecturesSearchRowSet> requestHandler3 = new TUMOnlineRequest<>(TUMOnlineConst.LECTURES_PERSONAL, mContext);
        if (!shouldRefresh(requestHandler3.getRequestURL())) {
            return;
        }

        Optional<LecturesSearchRowSet> lecturesList = requestHandler3.fetch();
        if (!lecturesList.isPresent()) {
            return;
        }
        List<LecturesSearchRow> lectures = lecturesList.get().getLehrveranstaltungen();
        if (lectures == null) {
            return;
        }
        ChatRoomManager manager = new ChatRoomManager(mContext);
        manager.replaceInto(lectures);

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

        CacheItem e = cacheBox.query().equal(CacheItem_.url, url).build().findFirst();
        if (e != null && isValidDate(e.getMaxAge())) {
            result = e.getData();
        }

        return Optional.fromNullable(result);
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date
     *
     * @param url Url from which data was cached
     * @return Data if valid version was found, null if no data is available
     */
    public boolean shouldRefresh(String url) {
        boolean result = true;

        CacheItem e = cacheBox.query().equal(CacheItem_.url, url).build().findFirst();
        if (e != null && isValidDate(e.getMaxAge())) {
            result = false;
        }

        return result;
    }

    /**
     * Add a result to cache
     *
     * @param url  url from where the data was fetched
     * @param data result
     */
    public void addToCache(String url, String data, int validity, int type) {
        if (validity == VALIDITY_DO_NOT_CACHE) {
            return;
        }
        DateTime maxAge = DateTime.now().plusSeconds(validity);
        CacheItem e = new CacheItem(url, data, validity, maxAge, type);
        this.cacheBox.put(e);
    }

    /**
     * @param onlyOld should all entries or only expired entries be removed?
     */
    public void clearCache(boolean onlyOld) {
        // Delete all entries that are too old and delete corresponding image files
        List<CacheItem> all = cacheBox.getAll();
        for (CacheItem e : all) {
            if (onlyOld && isValidDate(e.getMaxAge())) {
                continue;
            }

            //Delete the file
            if (e.getTyp() == CACHE_TYP_IMAGE) {
                File f = new File(e.getData());
                f.delete();
            }

            //Remove from db
            cacheBox.remove(e);
        }
    }

    public boolean isValidDate(DateTime e) {
        if (e.isAfterNow()) { //After now means the date is in the future
            return true;
        }
        return false;
    }
}