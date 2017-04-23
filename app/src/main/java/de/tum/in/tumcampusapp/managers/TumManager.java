package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import org.joda.time.DateTime;
import org.simpleframework.xml.core.Persister;

import java.util.List;

import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.entities.TcaBoxes;
import de.tum.in.tumcampusapp.entities.TumLock;
import de.tum.in.tumcampusapp.entities.TumLock_;
import de.tum.in.tumcampusapp.models.tumo.Error;
import io.objectbox.Box;

/**
 * TUMOnline lock manager: prevent too many requests send to TUMO
 */
public class TumManager extends AbstractManager {

    private Box<TumLock> lockBox;

    private static final int MAX_AGE = CacheManager.VALIDITY_ONE_DAY / 4; //Maximum length of a lock
    private static final int DEFAULT_LOCK = 60; //Base value for the first error: 60 seconds


    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public TumManager(Context context) {
        super(context);

        lockBox = TcaBoxes.getBoxStore().boxFor(TumLock.class);

        // Delete obsolete entries
        this.deleteExpired();
    }

    private void deleteExpired() {
        List<TumLock> all = lockBox.getAll();
        for (TumLock e : all) {
            if (e.getTimestamp().plusSeconds(e.getLockedFor()).isBeforeNow()) {
                lockBox.remove(e);
            }
        }
    }

    public String checkLock(String url) {
        //Deactivate all expired locks
        this.deleteExpired();

        //Try to get a result
        TumLock r = this.getLock(url);

        //If we got nothing there is no lock - or if it isn't active
        if (r == null) {
            return null;
        }

        //Otherwise return the error message
        return r.getError();
    }

    public void releaseLock(String url) {
        lockBox.remove(getLock(url));
    }

    public TumLock getLock(String url) {
        return lockBox.query().equal(TumLock_.url, url).build().findFirst();
    }

    public String addLock(String url, String data) {
        //Check if we have a lock already
        TumLock r = this.getLock(url);
        if (r == null) {
            r = new TumLock();
            r.setLockedFor(TumManager.DEFAULT_LOCK);
            r.setUrl(url);
        } else {
            //Double the lock time with each failed request
            r.setLockedFor(r.getLockedFor() * 2);

            //If we are above the limit reset to the limit
            if (r.getLockedFor() > TumManager.MAX_AGE) {
                r.setLockedFor(TumManager.MAX_AGE);
            }
        }

        //Try to parse the error
        String msg = "";
        try {
            Error res = new Persister().read(Error.class, data);
            r.setError(res.getMessage());
        } catch (Exception e) {
            Utils.log("Error getting message for TumLock: " + e.getMessage());
        }

        //Enter it into the Databse
        r.setTimestamp(DateTime.now());
        lockBox.put(r); //Objectbox automatically updates the entries as it detects that it already has an ID assigned or not

        return msg;
    }


}