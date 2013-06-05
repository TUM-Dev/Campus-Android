package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.CafeteriaManager;
import de.tum.in.tumcampusapp.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampusapp.models.managers.EventManager;
import de.tum.in.tumcampusapp.models.managers.FeedItemManager;
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

	private void broadcastError(String message) {
		Intent intentSend = new Intent();
		intentSend.setAction(broadcast);
		intentSend.putExtra(Const.ACTION_EXTRA, Const.ERROR);
		intentSend.putExtra(Const.ERROR_MESSAGE, message);
		sendBroadcast(intentSend);
	}

	public boolean downloadCafeterias() throws Exception {
		CafeteriaManager cm = new CafeteriaManager(this);
		CafeteriaMenuManager cmm = new CafeteriaMenuManager(this);
		cm.downloadFromExternal();
		cmm.downloadFromExternal();
		return true;
	}

	public boolean downloadEvents(boolean force) throws Exception {
		EventManager em = new EventManager(this);
		em.downloadFromExternal(force);
		return true;
	}

	public boolean downloadFeed(int feedId) throws Exception {
		FeedItemManager fim = new FeedItemManager(this);
		fim.downloadFromExternal(feedId, false);
		return true;
	}

	public boolean downloadGallery() throws Exception {
		GalleryManager gm = new GalleryManager(this);
		gm.downloadFromExternal();
		return true;
	}

	public boolean downloadNews() throws Exception {
		NewsManager nm = new NewsManager(this);
		nm.downloadFromExternal();
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
			broadcastError(e.getMessage());
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
		boolean force = intent.getBooleanExtra(Const.FORCE_DOWNLOAD, false);

		if (action == null) {
			// No action: leave service
			return;
		}

		Log.i(getClass().getSimpleName(), "Handle action <" + action + ">");

		try {
			if ((action.equals(Const.NEWS)) && !isDestroyed) {
				scucessfull = downloadNews();
			}
			if ((action.equals(Const.GALLERY)) && !isDestroyed) {
				scucessfull = downloadGallery();
			}
			if ((action.equals(Const.FEEDS)) && !isDestroyed) {
				int feedId = intent.getExtras().getInt(Const.FEED_ID);
				scucessfull = downloadFeed(feedId);
			}
			if ((action.equals(Const.CAFETERIAS)) && !isDestroyed) {
				scucessfull = downloadCafeterias();
			}
			if ((action.equals(Const.EVENTS)) && !isDestroyed) {
				scucessfull = downloadEvents(force);
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Error while handling action <" + action + ">");
			Log.e(getClass().getSimpleName(), "Problem Message: " + e.getMessage());
			broadcastError(e.getMessage());
		}

		// After done the job, create an broadcast intent and send it. The
		// receivers will be informed that the download service has finished.
		if (scucessfull) {
			broadcastDownloadCompleted();
		}
	}
}