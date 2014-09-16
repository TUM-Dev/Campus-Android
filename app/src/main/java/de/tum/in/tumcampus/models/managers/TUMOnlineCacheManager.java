package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;

import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class TUMOnlineCacheManager {
    private static final int TIME_TO_SYNC_CALENDAR = 5 * 86400000;
    public static int TIME_TO_SYNC_LECTURES = 86400000; // 1 day
    public static int TIME_TO_INVALID = 2*86400000; // 2 day

	/**
	 * Database connection
	 */
	private final SQLiteDatabase db;
    private Context mContext;

    /**
	 * Constructor, open/create database, create table if necessary
	 *
	 * <pre>
	 * @param context Context
	 * </pre>
	 */
	public TUMOnlineCacheManager(Context context) {
        mContext = context;
		db = DatabaseManager.getDb(context);

        // create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS tumonline (url VARCHAR UNIQUE, data BLOB, lastSync VARCHAR)");
        db.rawQuery("DELETE FROM tumonline WHERE lastSync > datetime('now', '-" + TIME_TO_INVALID + " second')", null);
	}

	/**
	 * Download usual tumonline requests
	 */
	public void fillCache() {
        // TODO cache news images

        // acquire access token
        if (new AccessTokenManager(mContext).hasValidAccessToken()) {
            return;
        }

        if (SyncManager.needSync(db, "lectures", TIME_TO_SYNC_LECTURES)) {
            // Sync lectures, details and appointments
            importLecturesFromTUMOnline();

            // Sync fee status
            TUMOnlineRequest requestHandler = new TUMOnlineRequest(Const.STUDIENBEITRAGSTATUS, mContext, true, true);
            requestHandler.fetch();
            SyncManager.replaceIntoDb(db, "lectures");
        }

        if (SyncManager.needSync(db, "calendar", TIME_TO_SYNC_CALENDAR)) {
            // Sync calendar
            TUMOnlineRequest requestHandler = new TUMOnlineRequest(Const.CALENDER, mContext, true, true);
            requestHandler.setParameter("pMonateVor", "0");
            requestHandler.setParameter("pMonateNach", "3");
            requestHandler.fetch();
            SyncManager.replaceIntoDb(db, "calendar");
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
            Cursor c = db.rawQuery("SELECT data FROM tumonline WHERE url=?", new String[] {url});
            if (c.getCount() == 1) {
                c.moveToFirst();
                result = c.getString(0);
            }
            c.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return result;
    }

	/**
	 * Removes all cache items
	 */
	public void removeCache() {
		db.execSQL("DELETE FROM tumonline");
	}

	/**
	 * Add a result to cache
	 * 
	 * <pre>
	 * @param url url from where the data was fetched
     * @param data result
	 * </pre>
	 */
	public void addToChache(String url, String data) {
		Utils.log("replace " + url + " " + data);
		db.execSQL("REPLACE INTO tumonline (url, data, lastSync) VALUES (?, ?, datetime())", new String[] { url, data });
	}


    /**
     * this function allows us to import all lecture settings from TUMOnline
     *
     * @throws Exception
     */
    public void importLecturesFromTUMOnline() {
        // get my lectures
        TUMOnlineRequest requestHandler = new TUMOnlineRequest(Const.LECTURES_PERSONAL, mContext, true, true);
        String strMine = requestHandler.fetch();
        // deserialize
        Serializer serializer = new Persister();

        // define it this way to get at least an empty list
        LecturesSearchRowSet myLecturesList = new LecturesSearchRowSet();
        myLecturesList.setLehrveranstaltungen(new ArrayList<LecturesSearchRow>());

        try {
            myLecturesList = serializer.read(LecturesSearchRowSet.class, strMine);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // get schedule for my lectures
        for (int i = 0; i < myLecturesList.getLehrveranstaltungen().size(); i++) {
            LecturesSearchRow currentLecture = myLecturesList.getLehrveranstaltungen().get(i);

            // now, get termine for each lecture
            TUMOnlineRequest req = new TUMOnlineRequest(Const.LECTURES_APPOINTMENTS, mContext, true, true);
            req.setParameter("pLVNr", currentLecture.getStp_sp_nr());
            req.fetch();

            req = new TUMOnlineRequest(Const.LECTURES_DETAILS, mContext, true, true);
            req.setParameter("pLVNr", currentLecture.getStp_sp_nr());
            req.fetch();
        }
    }
}