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
                f.getParentFile()
                 .mkdirs();
                globalDb = SQLiteDatabase.openOrCreateDatabase(f, null);
            }
            return globalDb;
        }
    }
}
