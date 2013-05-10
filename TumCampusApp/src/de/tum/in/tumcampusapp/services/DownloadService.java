package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.GalleryManager;
import de.tum.in.tumcampusapp.models.managers.NewsManager;
import de.tum.in.tumcampusapp.models.managers.SyncManager;

/**
 * Service used to download files from external pages
 */
public class DownloadService extends IntentService {

	/**
	 * Download broadcast identifier
	 */
	public final static String broadcast = "de.tum.in.newtumcampus.intent.action.BROADCAST_DOWNLOAD";
	private static final String DOWNLOAD_SERVICE = "DownloadService";
	/**
	 * Indicator to avoid starting new downloads
	 */
	private volatile boolean isDestroyed = false;

	/**
	 * default init (run intent in new thread)
	 */
	public DownloadService() {
		super(DOWNLOAD_SERVICE);
	}

	private void broadcastDownloadCompleted() {
		Intent intentSend = new Intent();
		intentSend.setAction(broadcast);
		intentSend.putExtra(Const.ACTION_EXTRA, Const.COMPLETED);
		sendBroadcast(intentSend);
	}

	private void broadcastError() {
		Intent intentSend = new Intent();
		intentSend.setAction(broadcast);
		intentSend.putExtra(Const.ACTION_EXTRA, Const.ERROR);
		sendBroadcast(intentSend);
	}

	public boolean downloadGallery() {
		GalleryManager gm = new GalleryManager(this);
		try {
			gm.downloadFromExternal();
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
			return false;
		}
		return true;
	}

	public boolean downloadNews() {
		NewsManager nm = new NewsManager(this);
		try {
			nm.downloadFromExternal();
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			// Check if sd card available
			Utils.getCacheDir("");
			// Init sync table
			new SyncManager(this);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
			broadcastError();
			// Don't start new downloads
			isDestroyed = true;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Don't start new downloads
		isDestroyed = true;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean scucessfull = false;
		String action = intent.getStringExtra(Const.ACTION_EXTRA);

		if (action == null) {
			// No action: leave service
			return;
		}

		Log.i(getClass().getSimpleName(), "Handle action <" + action + ">");

		if ((action.equals(Const.NEWS)) && !isDestroyed) {
			scucessfull = downloadNews();
		}
		if ((action.equals(Const.GALLERY)) && !isDestroyed) {
			scucessfull = downloadGallery();
		}

		// After done the job, create an broadcast intent and send it. The
		// receivers will be informed that the download service has finished.
		if (scucessfull) {
			broadcastDownloadCompleted();
		} else {
			Log.e(getClass().getSimpleName(), "Error while handling action <" + action + ">");
			broadcastError();
		}
	}
}