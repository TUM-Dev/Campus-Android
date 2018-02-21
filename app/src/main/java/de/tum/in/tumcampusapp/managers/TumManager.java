package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import org.simpleframework.xml.core.Persister;

import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dao.TumLockDao;
import de.tum.in.tumcampusapp.models.dbEntities.TumLock;
import de.tum.in.tumcampusapp.models.tumo.Error;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;

/**
 * TUMOnline lock manager: prevent too many requests send to TUMO
 */
public class TumManager {

    public static final int MAX_AGE = CacheManager.VALIDITY_ONE_DAY / 4; //Maximum length of a lock
    private static final int DEFAULT_LOCK = 60; //Base value for the first error: 60 seconds

    private TumLockDao dao;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public TumManager(Context context) {
        dao = TcaDb.getInstance(context)
                   .tumLockDao();
    }

    public String checkLock(String url) {
        dao.deactivateExpired();

        //Try to get a result
        TumLock r = dao.getFromUrl(url);

        //If we got nothing there is no lock - or if it isn't active
        if (r == null || r.getActive() == 0) {
            return null;
        }

        //Otherwise return the error message
        return r.getError();
    }

    public void releaseLock(String url) {
        TumLock lock = dao.getFromUrl(url);
        if (lock != null) {
            lock.setActive(0);
            dao.releaseLock(lock);
        }
    }

    public String addLock(String url, String data) {
        //Check if we have a lock already
        TumLock r = dao.getFromUrl(url);
        int lockTime = TumManager.DEFAULT_LOCK;
        if (r != null) {
            //Double the lock time with each failed request
            lockTime = r.getLockedFor() * 2;

            //If we are above the limit reset to the limit
            if (lockTime > TumManager.MAX_AGE) {
                lockTime = TumManager.MAX_AGE;
            }
        }

        //Try to parse the error
        String msg = "";
        try {
            if (data.contains(TUMOnlineRequest.NO_ENTRIES)) {
                return data;
            }
            Error res = new Persister().read(Error.class, data);
            msg = res.getMessage();
        } catch (Exception e) {
            Utils.log(e);
        }

        //Enter it into the Database
        dao.setLock(TumLock.Companion.create(url, msg, lockTime));
        return msg;
    }
}
