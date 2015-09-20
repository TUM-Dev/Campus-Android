package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.Location;
import de.tum.in.tumcampus.models.managers.CacheManager;
import de.tum.in.tumcampus.models.managers.CafeteriaManager;
import de.tum.in.tumcampus.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.KinoManager;
import de.tum.in.tumcampus.models.managers.NewsManager;
import de.tum.in.tumcampus.models.managers.OpenHoursManager;
import de.tum.in.tumcampus.models.managers.SyncManager;
import de.tum.in.tumcampus.trace.G;
import de.tum.in.tumcampus.trace.Util;

/**
 * Service used to download files from external pages
 */
public class DownloadService extends IntentService {

    /**
     * Download broadcast identifier
     */
    public final static String BROADCAST_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_DOWNLOAD";
    private static final String DOWNLOAD_SERVICE = "DownloadService";
    private static final String LAST_UPDATE = "last_update";
    private static final String CSV_LOCATIONS = "locations.csv";

    private static final Semaphore sem = new Semaphore(1, true);

    /**
     * default init (run intent in new thread)
     */
    public DownloadService() {
        super(DOWNLOAD_SERVICE);
    }

    /**
     * Gets the time when BackgroundService was called last time
     *
     * @param c Context
     * @return time when BackgroundService was executed last time
     */
    public static long lastUpdate(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(Const.INTERNAL_PREFS, 0);
        return prefs.getLong(LAST_UPDATE, 0);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.log("DownloadService service has started");

        // Init sync table
        new SyncManager(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.log("DownloadService service has stopped");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    download(intent);
                }catch (Exception e){
                    Utils.log("Could not acquire lock!");
                }
            }
        }).start();
    }

    private void download(Intent intent) throws InterruptedException {
        //Semaphore
        sem.acquire();

        //Set the app version if not set
        PackageInfo pi = Util.getPackageInfo(this);
        if (pi != null) {
            G.appVersion = pi.versionName; // Version
            G.appPackage = pi.packageName; // Package name
            G.appVersionCode = pi.versionCode; //Version code e.g.: 45
        }


        boolean successful = true;
        String action = intent.getStringExtra(Const.ACTION_EXTRA);
        boolean force = intent.getBooleanExtra(Const.FORCE_DOWNLOAD, false);
        boolean launch = intent.getBooleanExtra(Const.APP_LAUNCHES, false);

        // No action: leave service
        if (action == null) {
            //Unlock semaphore and return
            sem.release();
            return;
        }

        // Check if device has a internet connection
        if (NetUtils.isConnected(this) && (launch || !NetUtils.isConnectedMobileData(this))) {
            Utils.logv("Handle action <" + action + ">");
            try {
                switch (action) {
                    case Const.DOWNLOAD_ALL_FROM_EXTERNAL:
                        try {
                            downloadNews(force);
                        } catch (Exception e) {
                            Utils.log(e);
                            successful = false;
                        }
                        try {
                            downloadCafeterias(force);
                        } catch (Exception e) {
                            Utils.log(e);
                            successful = false;
                        }
                        try {
                            downLoadKino(force);
                        } catch (Exception e){
                            Utils.log(e);
                            successful = false;
                        }

                        boolean isSetup = Utils.getInternalSettingBool(this, Const.EVERYTHING_SETUP, false);
                        if (!isSetup) {
                            CacheManager cm = new CacheManager(this);
                            cm.syncCalendar();
                            if (successful)
                                Utils.setInternalSetting(this, Const.EVERYTHING_SETUP, true);
                        }
                        break;
                    case Const.NEWS:
                        downloadNews(force);
                        break;
                    case Const.CAFETERIAS:
                        downloadCafeterias(force);
                        break;
                    case Const.KINO:
                        downLoadKino(force);
                        break;
                }
            } catch (TimeoutException e) {
                Utils.log(e);
                broadcastWarning(getResources().getString(R.string.exception_timeout));
                successful = false;
            } catch (IOException e) {
                Utils.log(e);
                broadcastError(getResources().getString(
                        R.string.exception_sdcard));
                successful = false;
            } catch (Exception e) {
                Utils.log(e, "Unknown error while handling action <" + action + ">");
                broadcastError(getResources().getString(R.string.exception_unknown));
                successful = false;
            }
        } else {
            successful = false;
        }

        if ((action.equals(Const.DOWNLOAD_ALL_FROM_EXTERNAL))) {
            try {
                importLocationsDefaults();
            } catch (Exception e) {
                Utils.log(e);
                successful = false;
            }
            if (successful) {
                SharedPreferences prefs = getSharedPreferences(Const.INTERNAL_PREFS, 0);
                prefs.edit().putLong(LAST_UPDATE, System.currentTimeMillis()).apply();
            }
            CardManager.update(this);
            successful = true;
        }

        // After done the job, create an broadcast intent and send it. The
        // receivers will be informed that the download service has finished.
        if (successful) {
            this.broadcastDownloadCompleted();
        }

        // Do all other import stuff that is not relevant for creating the viewing the start page
        if ((action.equals(Const.DOWNLOAD_ALL_FROM_EXTERNAL))) {
            this.startService(new Intent(this, FillCacheService.class));
        }

        //Unlock semaphore and return
        sem.release();
    }

    private void broadcastDownloadCompleted() {
        Intent intentSend = new Intent();
        intentSend.setAction(BROADCAST_NAME);
        intentSend.putExtra(Const.ACTION_EXTRA, Const.COMPLETED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentSend);
    }

    private void broadcastError(String message) {
        Intent intentSend = new Intent();
        intentSend.setAction(BROADCAST_NAME);
        intentSend.putExtra(Const.ACTION_EXTRA, Const.ERROR);
        intentSend.putExtra(Const.ERROR_MESSAGE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentSend);
    }

    private void broadcastWarning(String message) {
        Intent intentSend = new Intent();
        intentSend.setAction(BROADCAST_NAME);
        intentSend.putExtra(Const.ACTION_EXTRA, Const.WARNING);
        intentSend.putExtra(Const.WARNING_MESSAGE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentSend);
    }

    private void downloadCafeterias(boolean force) throws Exception {
        CafeteriaManager cm = new CafeteriaManager(this);
        CafeteriaMenuManager cmm = new CafeteriaMenuManager(this);
        cm.downloadFromExternal(force);
        cmm.downloadFromExternal(this, force);
    }

    private void downLoadKino(boolean force) throws Exception {
        KinoManager km = new KinoManager(this);
        km.downloadFromExternal(force);
    }

    private void downloadNews(boolean force) throws Exception {
        NewsManager nm = new NewsManager(this);
        nm.downloadFromExternal(force);
    }

    /**
     * Import default location and opening hours from assets
     */
    private void importLocationsDefaults() throws Exception {
        OpenHoursManager lm = new OpenHoursManager(this);
        if (lm.empty()) {
            List<String[]> rows = Utils.readCsv(getAssets().open(CSV_LOCATIONS));

            for (String[] row : rows) {
                lm.replaceIntoDb(new Location(Integer.parseInt(row[0]), row[1],
                        row[2], row[3], row[4], row[5], row[6], row[7], row[8]));
            }
        }
    }
}