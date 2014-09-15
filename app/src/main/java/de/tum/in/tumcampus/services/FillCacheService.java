package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.content.Intent;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.TUMOnlineCacheManager;

/** Service used to sync data in background */
public class FillCacheService extends IntentService {

	public static final String CACHE_SERVICE = "FillCacheService";

	public FillCacheService() {
		super(CACHE_SERVICE);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.log("FillCacheService has started");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.log("FillCacheService has stopped");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        TUMOnlineCacheManager cache = new TUMOnlineCacheManager(this);
        cache.fillCache();
	}
}