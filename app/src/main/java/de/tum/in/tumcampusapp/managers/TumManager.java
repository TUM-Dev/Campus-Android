package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import org.simpleframework.xml.core.Persister;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.DateUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumo.Error;

/**
 * TUMOnline lock manager: prevent too many requests send to TUMO
 */
public class TumManager extends AbstractManager {

    private static final int colUrl = 0;
    private static final int colError = 1;
    private static final int colTimestamp = 2;
    private static final int colLockedFor = 3;
    private static final int colActive = 4;
    private static final int maxAge = CacheManager.VALIDITY_ONE_DAY / 4; //Maximum length of a lock
    private static final int defaultLock = 60; //Base value for the first error: 60 seconds

    public static class reqStatus {
        private String url;
        public String error;
        private Date timestamp;
        private int lockedFor;
        private int active;
    }

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public TumManager(Context context) {
        super(context);
        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS tumLocks (url VARCHAR UNIQUE, error VARCHAR, timestamp VARCHAR, lockedFor INT, active INT)");

        // Delete obsolete entries
        db.execSQL("DELETE FROM tumLocks WHERE datetime() > datetime(strftime('%s',timestamp) + " + TumManager.maxAge + ", 'unixepoch') AND active=0");
    }

    public String checkLock(String url) {
        //Deactivate all expired locks
        db.execSQL("UPDATE tumLocks SET active=0 WHERE datetime() > datetime(strftime('%s',timestamp) + lockedFor, 'unixepoch') AND active=1");

        //Try to get a result
        reqStatus r = this.getLock(url);

        //If we got nothing there is no lock - or if it isn't active
        if (r == null || r.active == 0) {
            return null;
        }

        //Otherwise return the error message
        return r.error;
    }

    public void releaseLock(String url) {
        //Enter it into the Databse
        try {
            db.execSQL("REPLACE INTO tumLocks (url,  active) VALUES (?, 0)", new String[]{url});
        } catch (SQLiteException e) {
            Utils.log(e);
        }
    }

    public reqStatus getLock(String url) {
        reqStatus result = null;

        try {
            Cursor c = db.rawQuery("SELECT * FROM tumLocks WHERE url=?", new String[]{url});
            if (c.getCount() == 1) {
                c.moveToFirst();
                result = new reqStatus();
                result.url = c.getString(colUrl);
                result.error = c.getString(colError);
                result.timestamp = DateUtils.parseSqlDate(c.getString(colTimestamp));
                result.lockedFor = c.getInt(colLockedFor);
                result.active = c.getInt(colActive);
            }
            c.close();
        } catch (SQLiteException e) {
            Utils.log(e);
        }
        return result;
    }

    public String addLock(String url, String data) {
        //Check if we have a lock already
        reqStatus r = this.getLock(url);
        int lockTime = TumManager.defaultLock;
        if (r != null) {
            //Double the lock time with each failed request
            lockTime = r.lockedFor * 2;

            //If we are above the limit reset to the limit
            if (lockTime > TumManager.maxAge) {
                lockTime = TumManager.maxAge;
            }
        }

        //Try to parse the error
        String msg = "";
        try {

            Error res = (new Persister()).read(Error.class, data);
            msg = res.getMessage();
        } catch (Exception e) {
            Utils.log(e);
        }

        //Enter it into the Databse
        db.execSQL("REPLACE INTO tumLocks (url, error, timestamp, lockedFor, active) VALUES (?, ?, datetime('now'), ?, 1)", new String[]{url, msg, String.valueOf(lockTime)});

        return msg;
    }


}