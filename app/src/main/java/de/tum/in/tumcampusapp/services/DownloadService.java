package de.tum.in.tumcampusapp.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dao.LocationDao;
import de.tum.in.tumcampusapp.managers.CacheManager;
import de.tum.in.tumcampusapp.managers.CafeteriaManager;
import de.tum.in.tumcampusapp.managers.CafeteriaMenuManager;
import de.tum.in.tumcampusapp.managers.CardManager;
import de.tum.in.tumcampusapp.managers.KinoManager;
import de.tum.in.tumcampusapp.managers.NewsManager;
import de.tum.in.tumcampusapp.managers.SurveyManager;
import de.tum.in.tumcampusapp.managers.SyncManager;
import de.tum.in.tumcampusapp.models.cafeteria.Location;
import de.tum.in.tumcampusapp.trace.G;
import de.tum.in.tumcampusapp.trace.Util;

import static de.tum.in.tumcampusapp.auxiliary.Const.DOWNLOAD_SERVICE_JOB_ID;

/**
 * Service used to download files from external pages
 */
public class DownloadService extends JobIntentService {

    /**
     * Download broadcast identifier
     */
    public final static String BROADCAST_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_DOWNLOAD";
    private static final String LAST_UPDATE = "last_update";
    private static final String CSV_LOCATIONS = "locations.csv";
    private LocalBroadcastManager broadcastManager;

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

    /**
     * Download the data for a specific intent
     * note, that only one concurrent download() is possible with a static synchronized method!
     */
    private static synchronized void download(Intent intent, DownloadService service) {
        //Set the app version if not set
        PackageInfo pi = Util.getPackageInfo(service);
        if (pi != null) {
            G.appVersion = pi.versionName; // Version
            G.appPackage = pi.packageName; // Package name
            G.appVersionCode = pi.versionCode; //Version code e.g.: 45
        }

        String action = intent.getStringExtra(Const.ACTION_EXTRA);

        // No action: leave service
        if (action == null) {
            return;
        }

        boolean successful = true;
        boolean force = intent.getBooleanExtra(Const.FORCE_DOWNLOAD, false);
        boolean launch = intent.getBooleanExtra(Const.APP_LAUNCHES, false);

        // Check if device has a internet connection

        boolean backgroundServicePermitted = Utils.isBackgroundServicePermitted(service);

        if (NetUtils.isConnected(service) && (launch || backgroundServicePermitted)) {
            Utils.logv("Handle action <" + action + ">");
            switch (action) {
                case Const.NEWS:
                    successful = service.downloadNews(force);
                    break;
                case Const.FACULTIES:
                    successful = service.downloadFacultiesAndSurveyData();
                    break;
                case Const.CAFETERIAS:
                    successful = service.downloadCafeterias(force);
                    break;
                case Const.KINO:
                    successful = service.downLoadKino(force);
                    break;
                case Const.DOWNLOAD_ALL_FROM_EXTERNAL:
                default:
                    successful = service.downloadAll(force);

                    boolean isSetup = Utils.getInternalSettingBool(service, Const.EVERYTHING_SETUP, false);
                    if (isSetup) {
                        break;
                    }
                    CacheManager cm = new CacheManager(service);
                    cm.syncCalendar();
                    if (successful) {
                        Utils.setInternalSetting(service, Const.EVERYTHING_SETUP, true);
                    }
                    break;
            }
        }

        // Update the last run time saved in shared prefs
        if (action.equals(Const.DOWNLOAD_ALL_FROM_EXTERNAL)) {
            try {
                service.importLocationsDefaults();
            } catch (IOException e) {
                Utils.log(e);
                successful = false;
            }
            if (successful) {
                SharedPreferences prefs = service.getSharedPreferences(Const.INTERNAL_PREFS, 0);
                prefs.edit()
                     .putLong(LAST_UPDATE, System.currentTimeMillis())
                     .apply();
            }
            CardManager.update(service);
            successful = true;
        }

        // After done the job, create an broadcast intent and send it. The receivers will be informed that the download service has finished.
        Utils.logv("Downloadservice was " + (successful ? "" : "not ") + "successful");
        if (successful) {
            service.broadcastDownloadCompleted();
        } else {
            service.broadcastError(service.getResources()
                                          .getString(R.string.exception_unknown));
        }

        // Do all other import stuff that is not relevant for creating the viewing the start page
        if (action.equals(Const.DOWNLOAD_ALL_FROM_EXTERNAL)) {
            FillCacheService.enqueueWork(service.getBaseContext(), new Intent());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.log("DownloadService service has started");
        broadcastManager = LocalBroadcastManager.getInstance(this);

        // Init sync table
        new SyncManager(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.log("DownloadService service has stopped");
    }

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, DownloadService.class, DOWNLOAD_SERVICE_JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull final Intent intent) {
        new Thread(() -> download(intent, DownloadService.this)).start();
    }

    private void broadcastDownloadCompleted() {
        sendServiceBroadcast(Const.COMPLETED, null);
    }

    private void broadcastError(String message) {
        sendServiceBroadcast(Const.ERROR, message);
    }

    private void sendServiceBroadcast(String actionExtra, String message) {
        Intent intentSend = new Intent(BROADCAST_NAME)
                .putExtra(Const.ACTION_EXTRA, actionExtra);
        if (message != null) {
            intentSend.putExtra(Const.MESSAGE, message);
        }

        if (broadcastManager != null) {
            broadcastManager.sendBroadcast(intentSend);
        }
    }

    /**
     * Download all external data and check, if the download was successful
     *
     * @param force True to force download over normal sync period
     * @return if all downloads were successful
     */
    private boolean downloadAll(boolean force) {
        final boolean cafe = downloadCafeterias(force);
        final boolean kino = downLoadKino(force);
        final boolean news = downloadNews(force);
        final boolean faculties = downloadFacultiesAndSurveyData();
        return cafe && kino && news && faculties;
    }

    private boolean downloadCafeterias(boolean force) {
        try {
            CafeteriaManager cm = new CafeteriaManager(this);
            CafeteriaMenuManager cmm = new CafeteriaMenuManager(this);
            cm.downloadFromExternal(force);
            cmm.downloadFromExternal(this, force);
            return true;
        } catch (JSONException e) {
            Utils.log(e);
            return false;
        }
    }

    private boolean downLoadKino(boolean force) {
        try {
            KinoManager km = new KinoManager(this);
            km.downloadFromExternal(force);
            return true;
        } catch (JSONException e) {
            Utils.log(e);
            return false;
        }
    }

    private boolean downloadNews(boolean force) {
        try {
            NewsManager nm = new NewsManager(this);
            nm.downloadFromExternal(force);
            return true;
        } catch (JSONException e) {
            Utils.log(e);
            return false;
        }
    }

    private boolean downloadFacultiesAndSurveyData() {
        SurveyManager sm = new SurveyManager(this);
        sm.downloadFacultiesFromExternal(); // Downloads the facultyData from the server in local db
        sm.downLoadOpenQuestions(); // Downloads openQuestions relevant for the survey card
        sm.downLoadOwnQuestions(); // Downloads ownQuestions relevant for displaying responses in surveyActivity
        return true;
    }

    /**
     * Import default location and opening hours from assets
     */
    private void importLocationsDefaults() throws IOException {
        LocationDao dao = TcaDb.getInstance(this)
                               .locationDao();
        if (dao.isEmpty()) {
            AssetManager assetManager = getAssets();
            List<String[]> rows = Utils.readCsv(assetManager.open(CSV_LOCATIONS));
            for (String[] row : rows) {
                dao.replaceInto(new Location(Integer.parseInt(row[0]), row[1],
                                             row[2], row[3], row[4], row[5], row[6], row[7], row[8]));
            }
        }
    }
}
