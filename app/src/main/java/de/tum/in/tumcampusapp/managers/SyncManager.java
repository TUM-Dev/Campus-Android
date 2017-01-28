package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import de.tum.in.tumcampusapp.entities.SyncItem;
import de.tum.in.tumcampusapp.entities.SyncItem_;
import de.tum.in.tumcampusapp.entities.TcaBoxes;
import io.objectbox.Box;

/**
 * Sync Manager, tracks last successful syncs
 */
public class SyncManager extends AbstractManager {

    private Box<SyncItem> syncBox;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public SyncManager(Context context) {
        super(context);
        syncBox = TcaBoxes.getBoxStore().boxFor(SyncItem.class);
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date
     *
     * @param obj     Gives class name as sync ID
     * @param seconds Sync period, e.g. 86400 for 1 day
     * @return true if sync is needed, else false
     */
    public boolean needSync(Object obj, int seconds) {
        return needSync(obj.getClass().getName(), seconds);
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date
     *
     * @param identifier      Sync-ID (derived by originator class name)
     * @param seconds Sync period, e.g. 86400 for 1 day
     * @return true if sync is needed, else false
     */
    public boolean needSync(String identifier, int seconds) {
        SyncItem e = syncBox.query().equal(SyncItem_.identifier, identifier).build().findFirst();
        if (e != null && e.getLastSync().plusSeconds(seconds).isAfterNow()) {
            return false;
        }

        return true;
    }

    /**
     * Replace or Insert a successful sync event in the database
     *
     * @param obj Gives class name as sync ID
     */
    public void replaceIntoDb(Object obj) {
        replaceIntoDb(obj.getClass().getName());
    }

    /**
     * Replace or Insert a successful sync event in the database
     *
     * @param identifier Sync-ID (derived by originator class name)
     */
    public void replaceIntoDb(String identifier) {
        if (identifier.isEmpty()) {
            return;
        }

        SyncItem e = new SyncItem(identifier);
        syncBox.put(e);
    }
}