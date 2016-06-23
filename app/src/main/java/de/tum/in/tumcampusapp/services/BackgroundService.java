package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.content.Intent;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.ImplicitCounter;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Service used to sync data in background
 * */
public class BackgroundService extends IntentService {

	private static final String BACKGROUND_SERVICE = "BackgroundService";

	public BackgroundService() {
		super(BACKGROUND_SERVICE);
	}

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

    /**
     * Starts {@link DownloadService} with appropriate extras
     * @param intent Intent
     */
	@Override
	protected void onHandleIntent(Intent intent) {
        // Download all from external
        Intent service = new Intent(this, DownloadService.class);
        service.putExtra(Const.ACTION_EXTRA, Const.DOWNLOAD_ALL_FROM_EXTERNAL);
        service.putExtra(Const.FORCE_DOWNLOAD, false);
        service.putExtra(Const.APP_LAUNCHES, intent.getBooleanExtra(Const.APP_LAUNCHES,false));
        startService(service);

        //Upload Usage statistics
        (new ImplicitCounter()).submitCounter(this);
	}
}