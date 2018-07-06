package de.tum.in.tumcampusapp.utils.sync;

import android.content.Context;

import org.joda.time.DateTime;

import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.sync.model.Sync;

/**
 * Sync Manager, tracks last successful syncs and prevents api fetch spams
 */
public class SyncManager {

    private final SyncDao dao;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public SyncManager(Context context) {
        dao = TcaDb.getInstance(context)
                   .syncDao();
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date
     *
     * @param obj     Gives class name as sync ID
     * @param seconds Sync period, e.g. 86400 for 1 day
     * @return true if sync is needed, else false
     */
    public boolean needSync(Object obj, int seconds) {
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
        return dao.getSyncSince(id, seconds) == null;
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
        if (id.isEmpty()) {
            return;
        }
        dao.insert(new Sync(id, DateTime.now()));
    }

    /**
     * Removes all items from database
     */
    public void deleteFromDb() {
        dao.removeCache();
    }
}