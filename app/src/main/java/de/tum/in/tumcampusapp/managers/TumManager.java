package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import org.simpleframework.xml.core.Persister;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.DateUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumo.Error;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;

/**
 * TUMOnline lock manager: prevent too many requests send to TUMO
 */
public class TumManager extends AbstractManager {

    private static final int COL_URL = 0;
    private static final int COL_ERROR = 1;
    private static final int COL_TIMESTAMP = 2;
    private static final int COL_LOCKED_FOR = 3;
    private static final int COL_ACTIVE = 4;
    private static final int MAX_AGE = CacheManager.VALIDITY_ONE_DAY / 4; //Maximum length of a lock
    private static final int DEFAULT_LOCK = 60; //Base value for the first error: 60 seconds

    public static class ReqStatus {
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
        db.execSQL("DELETE FROM tumLocks WHERE datetime() > datetime(strftime('%s',timestamp) + " + TumManager.MAX_AGE + ", 'unixepoch') AND active=0");
    }

    public String checkLock(String url) {
        //Deactivate all expired locks
        db.execSQL("UPDATE tumLocks SET active=0 WHERE datetime() > datetime(strftime('%s',timestamp) + lockedFor, 'unixepoch') AND active=1");

        //Try to get a result
        ReqStatus r = this.getLock(url);

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

    public ReqStatus getLock(String url) {
        ReqStatus result = null;

        try (Cursor c = db.rawQuery("SELECT * FROM tumLocks WHERE url=?", new String[]{url})) {
            if (c.getCount() == 1) {
                c.moveToFirst();
                result = new ReqStatus();
                result.url = c.getString(COL_URL);
                result.error = c.getString(COL_ERROR);
                result.timestamp = DateUtils.parseSqlDate(c.getString(COL_TIMESTAMP));
                result.lockedFor = c.getInt(COL_LOCKED_FOR);
                result.active = c.getInt(COL_ACTIVE);
            }

        } catch (SQLiteException e) {
            Utils.log(e);
        }
        return result;
    }

    public String addLock(String url, String data) {
        //Check if we have a lock already
        ReqStatus r = this.getLock(url);
        int lockTime = TumManager.DEFAULT_LOCK;
        if (r != null) {
            //Double the lock time with each failed request
            lockTime = r.lockedFor * 2;

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
        db.execSQL("REPLACE INTO tumLocks (url, error, timestamp, lockedFor, active) VALUES (?, ?, datetime('now'), ?, 1)", new String[]{url, msg, String.valueOf(lockTime)});

        return msg;
    }

}