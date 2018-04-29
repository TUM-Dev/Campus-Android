package de.tum.in.tumcampusapp.service;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaLocationDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager;
import de.tum.in.tumcampusapp.component.ui.cafeteria.details.CafeteriaViewModel;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.Location;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository;
import de.tum.in.tumcampusapp.component.ui.news.KinoViewModel;
import de.tum.in.tumcampusapp.component.ui.news.NewsController;
import de.tum.in.tumcampusapp.component.ui.news.TopNewsViewModel;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoLocalRepository;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoRemoteRepository;
import de.tum.in.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.CacheManager;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import de.tum.in.tumcampusapp.utils.sync.SyncManager;
import io.reactivex.disposables.CompositeDisposable;

import static de.tum.in.tumcampusapp.utils.Const.DOWNLOAD_SERVICE_JOB_ID;

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
    private CafeteriaViewModel cafeteriaViewModel;

    private CompositeDisposable mDisposable = new CompositeDisposable();
    private KinoViewModel kinoViewModel;
    private TopNewsViewModel topNewsViewModel;

    /**
     * Gets the time when BackgroundService was called last time
     *
     * @param context Context
     * @return time when BackgroundService was executed last time
     */
    public static long lastUpdate(Context context) {
        return Utils.getSettingLong(context, LAST_UPDATE, 0L);
    }

    /**
     * Download the data for a specific intent
     * note, that only one concurrent download() is possible with a static synchronized method!
     */
    private static synchronized void download(Intent intent, DownloadService service) {
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
                case Const.CAFETERIAS:
                    successful = service.downloadCafeterias(force);
                    break;
                case Const.KINO:
                    successful = service.downLoadKino(force);
                    break;
                case Const.TOP_NEWS:
                    successful = service.dowloadTopNews();
                    break;
                case Const.DOWNLOAD_ALL_FROM_EXTERNAL:
                default:
                    successful = service.downloadAll(force);

                    boolean isSetup = Utils.getSettingBool(service, Const.EVERYTHING_SETUP, false);
                    if (isSetup) {
                        break;
                    }
                    CacheManager cm = new CacheManager(service);
                    cm.syncCalendar();
                    if (successful) {
                        Utils.setSetting(service, Const.EVERYTHING_SETUP, true);
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
                Utils.setSetting(service, LAST_UPDATE, System.currentTimeMillis());
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

        new SyncManager(this);

        CafeteriaRemoteRepository remoteRepository = CafeteriaRemoteRepository.INSTANCE;
        remoteRepository.setTumCabeClient(TUMCabeClient.getInstance(this));
        CafeteriaLocalRepository localRepository = CafeteriaLocalRepository.INSTANCE;
        localRepository.setDb(TcaDb.getInstance(this));
        cafeteriaViewModel = new CafeteriaViewModel(localRepository, remoteRepository, mDisposable);

        // Init sync table
        KinoLocalRepository.INSTANCE.setDb(TcaDb.getInstance(this));
        KinoRemoteRepository.INSTANCE.setTumCabeClient(TUMCabeClient.getInstance(this));
        kinoViewModel = new KinoViewModel(KinoLocalRepository.INSTANCE, KinoRemoteRepository.INSTANCE, mDisposable);

        TopNewsRemoteRepository.INSTANCE.setTumCabeClient(TUMCabeClient.getInstance(this));
        topNewsViewModel = new TopNewsViewModel(TopNewsRemoteRepository.INSTANCE, mDisposable);
    }

    @Override
    public void onDestroy() {
        mDisposable.clear();
        super.onDestroy();
        Utils.log("DownloadService service has stopped");
    }

    public static void enqueueWork(Context context, Intent work) {
        Utils.log("Download work enqueued");
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
        final boolean topNews = dowloadTopNews();
        return cafe && kino && news && topNews;
    }

    private boolean downloadCafeterias(boolean force) {
        CafeteriaMenuManager cmm = new CafeteriaMenuManager(this);
        cmm.downloadFromExternal(this, force);
        cafeteriaViewModel.getCafeteriasFromService(force);
        return true;
    }

    private boolean downLoadKino(boolean force) {
        kinoViewModel.getKinosFromService(force);
        return true;
    }

    private boolean downloadNews(boolean force) {
        try {
            NewsController nm = new NewsController(this);
            nm.downloadFromExternal(force);
            return true;
        } catch (JSONException e) {
            Utils.log(e);
            return false;
        }
    }

    private boolean dowloadTopNews(){
        return topNewsViewModel.getNewsAlertFromService(getApplicationContext());
    }

    /**
     * Import default location and opening hours from assets
     */
    private void importLocationsDefaults() throws IOException {
        CafeteriaLocationDao dao = TcaDb.getInstance(this)
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
