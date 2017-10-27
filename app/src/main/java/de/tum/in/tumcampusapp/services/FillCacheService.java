package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.content.Intent;

import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.CacheManager;

/**
 * Service used to fill caches in background, for faster/offline access
 **/
public class FillCacheService extends IntentService {

    private static final String CACHE_SERVICE = "FillCacheService";

    public FillCacheService() {
        super(CACHE_SERVICE);
    }

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

    @Override
    protected void onHandleIntent(Intent intent) {
        new Thread(() -> {
            // Fill cache service
            CacheManager cache = new CacheManager(FillCacheService.this);
            cache.fillCache();
        }).start();
    }
}