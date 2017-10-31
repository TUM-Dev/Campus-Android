package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Sync Manager, tracks last successful syncs
 */
public class SyncManager extends AbstractManager {

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public SyncManager(Context context) {
        super(context);
        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS syncs (id VARCHAR PRIMARY KEY, lastSync VARCHAR)");
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date
     *
     * @param obj     Gives class name as sync ID
     * @param seconds Sync period, e.g. 86400 for 1 day
     * @return true if sync is needed, else false
     */
    boolean needSync(Object obj, int seconds) {
        return needSync(obj.getClass()
                           .getName(), seconds);
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date
     *
     * @param id      Sync-ID (derived by originator class name)
     * @param seconds Sync period, e.g. 86400 for 1 day
     * @return true if sync is needed, else false
     */
    public boolean needSync(String id, int seconds) {
        boolean result = true;

        try (Cursor c = db.rawQuery("SELECT lastSync FROM syncs WHERE lastSync > datetime('now', '-" + seconds + " second') AND id=?", new String[]{id})) {
            if (c.getCount() == 1) {
                result = false;
            }
        } catch (SQLiteException e) {
            if (e.getMessage()
                 .contains("no such table")) {
                Utils.log("Error selecting table syncs because it doesn't exist!");
                return true;
            }
        }
        return result;
    }

    /**
     * Replace or Insert a successful sync event in the database
     *
     * @param obj Gives class name as sync ID
     */
    public void replaceIntoDb(Object obj) {
        replaceIntoDb(obj.getClass()
                         .getName());
    }

    /**
     * Replace or Insert a successful sync event in the database
     *
     * @param id Sync-ID (derived by originator class name)
     */
    public void replaceIntoDb(String id) {
        Utils.log(id);

        if (id.isEmpty()) {
            return;
        }
        db.execSQL("REPLACE INTO syncs (id, lastSync) VALUES (?, datetime())",
                   new String[]{id});
    }

    /**
     * Removes all items from database
     */
    public void deleteFromDb() {
        db.execSQL("DELETE FROM syncs");
    }
}