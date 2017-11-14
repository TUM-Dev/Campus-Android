package de.tum.in.tumcampusapp.services;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.CacheManager;

import static de.tum.in.tumcampusapp.auxiliary.Const.FILL_CACHE_SERVICE_JOB_ID;

/**
 * Service used to fill caches in background, for faster/offline access
 **/
public class FillCacheService extends JobIntentService {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.logv("FillCacheService has started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.logv("FillCacheService has stopped");
    }

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, FillCacheService.class, FILL_CACHE_SERVICE_JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        new Thread(() -> {
            // Fill cache service
            CacheManager cache = new CacheManager(FillCacheService.this);
            cache.fillCache();
        }).start();
    }
}