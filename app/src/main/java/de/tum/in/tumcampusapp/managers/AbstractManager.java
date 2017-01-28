package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import de.tum.in.tumcampusapp.auxiliary.Const;

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
                f.getParentFile().mkdirs();
                globalDb = SQLiteDatabase.openDatabase(f.toString(),
                        null, SQLiteDatabase.CREATE_IF_NECESSARY);
            }
            return globalDb;
        }
    }

    public static void resetDb(Context c) {
        SQLiteDatabase db = getDb(c);

        //Cafeteria / OpenHours managers
        db.execSQL("DROP TABLE IF EXISTS cafeterias");
        db.execSQL("DROP TABLE IF EXISTS cafeterias_menus");
        db.execSQL("DROP TABLE IF EXISTS locations");

        //Cal manager
        db.execSQL("DROP TABLE IF EXISTS calendar");
        db.execSQL("DROP TABLE IF EXISTS room_locations");

        //news manager
        db.execSQL("DROP TABLE IF EXISTS news");
        db.execSQL("DROP TABLE IF EXISTS news_sources");

        //Chat manager
        db.execSQL("DROP TABLE IF EXISTS unsent_chat_message");
        db.execSQL("DROP TABLE IF EXISTS chat_message");
        db.execSQL("DROP TABLE IF EXISTS chat_room");
    }
}
