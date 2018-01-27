package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.services.BackgroundService;
import de.tum.in.tumcampusapp.services.DownloadService;
import de.tum.in.tumcampusapp.services.SendMessageService;
import de.tum.in.tumcampusapp.services.SilenceService;

public class AbstractManager {
    protected Context mContext;
    private static final Object GLOBAL_DB_LOCK = new Object();
    private static SQLiteDatabase globalDb;
    protected final SQLiteDatabase db;

    protected AbstractManager(Context context) {
        mContext = context.getApplicationContext();
        db = getDb(context);
    }

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param c Context
     * @return SQLiteDatabase Db
     */
    public static SQLiteDatabase getDb(Context c) { // TODO: create a suggestionsmanager and make this protected
        synchronized (GLOBAL_DB_LOCK) {
            if (globalDb == null) {
                File f = c.getDatabasePath(Const.DATABASE_NAME);
                f.getParentFile()
                 .mkdirs();
                globalDb = SQLiteDatabase.openOrCreateDatabase(f, null);
            }
            return globalDb;
        }
    }

    /**
     * Drop all tables, so we can do a complete clean start
     * Careful: After executing this method, almost all the managers are in an illegal state, and
     * can't do any SQL anymore. So take care to actually reinitialize all Managers
     *
     * @param c context
     */
    public static void resetDb(Context c) {
        SQLiteDatabase db = getDb(c);

        // Stop all services, since they might have instantiated Managers and cause SQLExceptions
        Class<?>[] services = new Class<?>[]{
                CalendarManager.QueryLocationsService.class,
                SendMessageService.class,
                SilenceService.class,
                DownloadService.class,
                BackgroundService.class};
        for (Class<?> service : services) {
            c.stopService(new Intent(c, service));
        }

        db.beginTransaction();
        try {

            db.execSQL("DROP TABLE IF EXISTS suggestions_lecture");
            db.execSQL("DROP TABLE IF EXISTS suggestions_mvv");
            db.execSQL("DROP TABLE IF EXISTS suggestions_persons");
            db.execSQL("DROP TABLE IF EXISTS suggestions_rooms");

            CacheManager.clearCache(c);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        TcaDb tdb = TcaDb.getInstance(c);
        tdb.cafeteriaDao().removeCache();
        tdb.cafeteriaMenuDao().removeCache();
        tdb.calendarDao().flush();
        tdb.locationDao().removeCache();
        tdb.newsDao().flush();
        tdb.newsSourcesDao().flush();
        tdb.recentsDao().removeCache();
        tdb.roomLocationsDao().flush();
        tdb.syncDao().removeCache();
        tdb.chatMessageDao().removeCache();
        tdb.chatRoomDao().removeCache();
        tdb.openQuestionsDao().flush();
        tdb.ownQuestionsDao().flush();
        tdb.tumLockDao().removeCache();
        tdb.facultyDao().flush();
        tdb.transportDao().removeCache();
        tdb.studyRoomDao().removeCache();
        tdb.studyRoomGroupDao().removeCache();
        tdb.kinoDao().flush();
        tdb.widgetsTimetableBlacklistDao().flush();
        tdb.notificationDao().cleanup();
        tdb.favoriteDishDao().removeCache();
        tdb.buildingToGpsDao().removeCache();
        tdb.wifiMeasurementDao().cleanup();
    }
}
