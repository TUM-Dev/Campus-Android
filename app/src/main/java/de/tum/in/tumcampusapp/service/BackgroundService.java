package de.tum.in.tumcampusapp.service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import de.tum.in.tumcampusapp.component.other.reporting.stats.ImplicitCounter;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

import static de.tum.in.tumcampusapp.utils.Const.BACKGROUND_SERVICE_JOB_ID;

/**
 * Service used to sync data in background
 */
public class BackgroundService extends JobIntentService {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.log("BackgroundService has started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.log("BackgroundService has stopped");
    }

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, BackgroundService.class, BACKGROUND_SERVICE_JOB_ID, work);
    }

    /**
     * Starts {@link DownloadService} with appropriate extras
     *
     * @param intent Intent
     */
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // Download all from external
        Intent service = new Intent();
        service.putExtra(Const.ACTION_EXTRA, Const.DOWNLOAD_ALL_FROM_EXTERNAL);
        service.putExtra(Const.FORCE_DOWNLOAD, false);
        service.putExtra(Const.APP_LAUNCHES, intent.getBooleanExtra(Const.APP_LAUNCHES, false));

        DownloadService.enqueueWork(getBaseContext(), service);

        //Upload Usage statistics
        ImplicitCounter.submitCounter(this);
    }
}