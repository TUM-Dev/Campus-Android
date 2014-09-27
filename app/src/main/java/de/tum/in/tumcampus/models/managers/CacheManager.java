package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.CalendarRowSet;
import de.tum.in.tumcampus.models.LectureAppointmentsRowSet;
import de.tum.in.tumcampus.models.LectureDetailsRowSet;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;
import de.tum.in.tumcampus.models.TuitionList;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class CacheManager {
    private static final int TIME_TO_SYNC_CALENDAR = 5 * 86400; // 5 days
    private static final int TIME_TO_SYNC_LECTURES = 86400; // 1 day
    private static final int TIME_TO_DELETE = 10*86400; // 10 days

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
        db.execSQL("DROP TABLE cache");
		db.execSQL("CREATE TABLE IF NOT EXISTS cache (url VARCHAR UNIQUE, data BLOB, lastSync VARCHAR, validity INTEGER)");
        db.rawQuery("DELETE FROM cache WHERE lastSync > datetime('now', '-"+TIME_TO_DELETE+" second')", null);
	}

	/**
	 * Download usual tumOnline requests
	 */
	public void fillCache() {
        // Cache webservice
        //TODO integrate sync manager into cache
        if (SyncManager.needSync(db, "curricula", TIME_TO_SYNC_LECTURES)) { //TODO reset if loaded from activity/does it load if too old?
            Utils.downloadJsonArray(mContext, "https://tumcabe.in.tum.de/Api/curricula", true);
            SyncManager.replaceIntoDb(db, "curricula");
        }

        // TODO cache news images

        // acquire access token
        if (new AccessTokenManager(mContext).hasValidAccessToken()) {
            return;
        }

        if (SyncManager.needSync(db, "lectures", TIME_TO_SYNC_LECTURES)) {
            // Sync lectures, details and appointments
            importLecturesFromTUMOnline();

            // Sync fee status
            TUMOnlineRequest<TuitionList> requestHandler = new TUMOnlineRequest<TuitionList>(TUMOnlineConst.STUDIENBEITRAGSTATUS, TuitionList.class, mContext);
            requestHandler.fetch();
            SyncManager.replaceIntoDb(db, "lectures");
        }

        if (SyncManager.needSync(db, "calendar", TIME_TO_SYNC_CALENDAR)) {
            // Sync calendar
            TUMOnlineRequest<CalendarRowSet> requestHandler = new TUMOnlineRequest<CalendarRowSet>(TUMOnlineConst.CALENDER, CalendarRowSet.class, mContext);
            requestHandler.setParameter("pMonateVor", "0");
            requestHandler.setParameter("pMonateNach", "3");

            CalendarManager calendarManager = new CalendarManager(mContext);
            CalendarRowSet set = requestHandler.fetch();
            if(set!=null) {
                calendarManager.importCalendar(set);
                SyncManager.replaceIntoDb(db, "calendar");
            }
        }

	}

    /**
     * Checks if a new sync is needed or if data is up-to-date
     *
     * @param url Url from which data was cached
     * @return Data if valid version was found, null if no data is available
     */
    public String getFromCache(String url) {
        String result = null;

        try {
            Cursor c = db.rawQuery("SELECT data FROM cache WHERE url=? AND lastSync > datetime('now', '-`validity` second')", new String[] {url});
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
	 * Add a result to cache
	 *
	 * @param url url from where the data was fetched
     * @param data result
	 */
	public void addToCache(String url, String data, int validity) {
		Utils.logv("replace " + url + " " + data);
		db.execSQL("REPLACE INTO cache (url, data, lastSync, validity) VALUES (?, ?, datetime(), ?)", new String[] { url, data, ""+validity });
	}

    /**
     * this function allows us to import all lecture items from TUMOnline
     */
    void importLecturesFromTUMOnline() {
        // get my lectures
        TUMOnlineRequest<LecturesSearchRowSet> requestHandler = new TUMOnlineRequest<LecturesSearchRowSet>(TUMOnlineConst.LECTURES_PERSONAL, LecturesSearchRowSet.class, mContext);
        LecturesSearchRowSet myLecturesList = requestHandler.fetch();
        if(myLecturesList==null)
            return;

        // get schedule for my lectures
        for (int i = 0; i < myLecturesList.getLehrveranstaltungen().size(); i++) {
            LecturesSearchRow currentLecture = myLecturesList.getLehrveranstaltungen().get(i);

            // now, get appointments for each lecture
            TUMOnlineRequest<LectureAppointmentsRowSet> req = new TUMOnlineRequest<LectureAppointmentsRowSet>(TUMOnlineConst.LECTURES_APPOINTMENTS, LectureAppointmentsRowSet.class, mContext);
            req.setParameter("pLVNr", currentLecture.getStp_sp_nr());
            req.fetch();

            TUMOnlineRequest<LectureDetailsRowSet> req2 = new TUMOnlineRequest<LectureDetailsRowSet>(TUMOnlineConst.LECTURES_DETAILS, LectureDetailsRowSet.class, mContext);
            req2.setParameter("pLVNr", currentLecture.getStp_sp_nr());
            req2.fetch();
        }
    }
}