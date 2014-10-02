package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.File;

import de.tum.in.tumcampus.activities.CurriculaActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.CalendarRowSet;
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

    /** Validity's for entries in seconds */
    public static final int VALIDITY_DO_NOT_CACHE = 0;
    public static final int VALIDITY_ONE_DAY = 86400;
    public static final int VALIDITY_TWO_DAYS = 2*86400;
    public static final int VALIDITY_FIFE_DAYS = 5*86400;
    public static final int VALIDITY_TEN_DAYS = 10*86400;
    public static final int VALIDITY_ONE_MONTH = 30*86400;

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
        Cursor cur = db.rawQuery("SELECT data FROM cache WHERE datetime()>max_age AND typ=1",null);
        if(cur.moveToFirst()) {
            do {
                File f = new File(cur.getString(0));
                f.delete();
            } while (cur.moveToNext());
        }
        cur.close();
        db.rawQuery("DELETE FROM cache WHERE datetime()>max_age", null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Download usual tumOnline requests
     */
    public void fillCache() {
        // Cache curricula urls
        if (shouldRefresh(CurriculaActivity.CURRICULA_URL)) {
            Utils.downloadJsonArray(mContext, CurriculaActivity.CURRICULA_URL, true, CacheManager.VALIDITY_ONE_MONTH);
        }

        // Cache news images
        NewsManager news = new NewsManager(mContext);
        Cursor cur = news.getAllFromDb(mContext);
        if(cur.moveToFirst()) {
            do {
                String imgUrl = cur.getString(5);
                if(!imgUrl.isEmpty())
                    Utils.downloadImage(mContext, imgUrl);
            } while(cur.moveToNext());
        }
        cur.close();

        // acquire access token
        if (!new AccessTokenManager(mContext).hasValidAccessToken()) {
            return;
        }

        // ALL STUFF BELOW HERE NEEDS A VALID ACCESS TOKEN

        // Sync organisation tree
        TUMOnlineRequest<OrgItemList> requestHandler = new TUMOnlineRequest<OrgItemList>(TUMOnlineConst.ORG_TREE, mContext);
        if (shouldRefresh(requestHandler.getRequestURL())) {
            requestHandler.fetch();
        }

        // Sync fee status
        TUMOnlineRequest<TuitionList> requestHandler2 = new TUMOnlineRequest<TuitionList>(TUMOnlineConst.TUITION_FEE_STATUS, mContext);
        if (shouldRefresh(requestHandler2.getRequestURL())) {
            requestHandler2.fetch();
        }

        // Sync lectures, details and appointments
        importLecturesFromTUMOnline();

        // Sync calendar
        TUMOnlineRequest<CalendarRowSet> requestHandler3 = new TUMOnlineRequest<CalendarRowSet>(TUMOnlineConst.CALENDER, mContext);
        requestHandler.setParameter("pMonateVor", "0");
        requestHandler.setParameter("pMonateNach", "3");
        if (shouldRefresh(requestHandler3.getRequestURL())) {
            CalendarRowSet set = requestHandler3.fetch();
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
        if(validity==VALIDITY_DO_NOT_CACHE)
            return;
        Utils.logv("replace " + url + " " + data);
        db.execSQL("REPLACE INTO cache (url, data, validity, max_age, typ) " +
                        "VALUES (?, ?, datetime('now','+"+(validity/2)+" seconds'), " +
                        "datetime('now','+"+validity+" seconds'), ?)",
                new String[]{url, data, "" + typ});
    }

    /**
     * this function allows us to import all lecture items from TUMOnline
     */
    void importLecturesFromTUMOnline() {
        // get my lectures
        TUMOnlineRequest<LecturesSearchRowSet> requestHandler = new TUMOnlineRequest<LecturesSearchRowSet>(TUMOnlineConst.LECTURES_PERSONAL, mContext);
        if(!shouldRefresh(requestHandler.getRequestURL()))
            return;

        //LecturesSearchRowSet myLecturesList =
                requestHandler.fetch();
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
}