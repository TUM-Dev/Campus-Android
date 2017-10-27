package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import de.tum.in.tumcampusapp.auxiliary.Const;
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
            db.execSQL("DROP TABLE IF EXISTS cafeterias");
            db.execSQL("DROP TABLE IF EXISTS cafeterias_menus");
            db.execSQL("DROP TABLE IF EXISTS calendar");
            db.execSQL("DROP TABLE IF EXISTS kalendar_events");
            db.execSQL("DROP TABLE IF EXISTS locations");
            db.execSQL("DROP TABLE IF EXISTS news");
            db.execSQL("DROP TABLE IF EXISTS news_sources");
            db.execSQL("DROP TABLE IF EXISTS recents");
            db.execSQL("DROP TABLE IF EXISTS room_locations");
            db.execSQL("DROP TABLE IF EXISTS syncs");
            db.execSQL("DROP TABLE IF EXISTS suggestions_lecture");
            db.execSQL("DROP TABLE IF EXISTS suggestions_mvv");
            db.execSQL("DROP TABLE IF EXISTS suggestions_persons");
            db.execSQL("DROP TABLE IF EXISTS suggestions_rooms");
            db.execSQL("DROP TABLE IF EXISTS unsent_chat_message");
            db.execSQL("DROP TABLE IF EXISTS chat_message");
            db.execSQL("DROP TABLE IF EXISTS chat_room");
            db.execSQL("DROP TABLE IF EXISTS tumLocks");
            db.execSQL("DROP TABLE IF EXISTS openQuestions");
            db.execSQL("DROP TABLE IF EXISTS ownQuestions");
            db.execSQL("DROP TABLE IF EXISTS faculties");
            db.execSQL("DROP TABLE IF EXISTS transport_favorites");
            CacheManager.clearCache(c);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
