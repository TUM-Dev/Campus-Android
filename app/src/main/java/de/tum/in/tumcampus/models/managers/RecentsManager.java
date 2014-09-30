package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Transport Manager, handles database stuff, internet connections
 */
public class RecentsManager {
    public static final int STATIONS = 1;
    public static final int ROOMS = 2;
    public static final int PERSONS = 3;

    /**
     * Typ to search for
     */
    private final int typ;

    /**
     * Database connection
     */
    private final SQLiteDatabase db;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public RecentsManager(Context context, int typ) {
        db = DatabaseManager.getDb(context);
        this.typ = typ;

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS recents (typ INTEGER, name VARCHAR UNIQUE)");
    }

    /**
     * Checks if the transports table is empty
     *
     * @return true if no stations are available, else false
     */
    public boolean empty() {
        boolean result = true;
        Cursor c = db.rawQuery("SELECT name FROM recents WHERE typ=? LIMIT 1", new String[]{""+typ});
        if (c.moveToNext()) {
            result = false;
        }
        c.close();
        return result;
    }

    /**
     * Get all recents from the database
     *
     * @return Database cursor (name, _id)
     */
    public Cursor getAllFromDb() {
        return db.rawQuery("SELECT name, name as _id FROM recents WHERE typ=? ORDER BY name", new String[]{"" + typ});
    }

    /**
     * Replace or Insert a item into the database
     *
     * @param name Recent name
     */
    public void replaceIntoDb(String name) {
        Utils.log(name);
        if (name.length() == 0) {
            return;
        }
        db.execSQL("REPLACE INTO recents (typ,name) VALUES (?,?)", new String[]{""+typ, name});
    }
}