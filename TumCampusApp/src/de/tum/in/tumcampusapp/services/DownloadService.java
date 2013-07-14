package de.tum.in.tumcampusapp.services;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.CafeteriaManager;
import de.tum.in.tumcampusapp.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampusapp.models.managers.EventManager;
import de.tum.in.tumcampusapp.models.managers.FeedItemManager;
import de.tum.in.tumcampusapp.models.managers.GalleryManager;
import de.tum.in.tumcampusapp.models.managers.NewsManager;
import de.tum.in.tumcampusapp.models.managers.OrganisationManager;
import de.tum.in.tumcampusapp.models.managers.SyncManager;

/**
 * Service used to download files from external pages
 */
public class DownloadService extends IntentService {

	/**
	 * Download broadcast identifier
	 */
	public final static String BROADCAST_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_DOWNLOAD";
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
		intentSend.setAction(BROADCAST_NAME);
		intentSend.putExtra(Const.ACTION_EXTRA, Const.COMPLETED);
		sendBroadcast(intentSend);
	}

	private void broadcastError(String message) {
		Intent intentSend = new Intent();
		intentSend.setAction(BROADCAST_NAME);
		intentSend.putExtra(Const.ACTION_EXTRA, Const.ERROR);
		intentSend.putExtra(Const.ERROR_MESSAGE, message);
		sendBroadcast(intentSend);
	}

	private void broadcastWarning(String message) {
		Intent intentSend = new Intent();
		intentSend.setAction(BROADCAST_NAME);
		intentSend.putExtra(Const.ACTION_EXTRA, Const.WARNING);
		intentSend.putExtra(Const.WARNING_MESSAGE, message);
		sendBroadcast(intentSend);
	}

	public boolean downloadCafeterias(boolean force) throws Exception {
		CafeteriaManager cm = new CafeteriaManager(this);
		CafeteriaMenuManager cmm = new CafeteriaMenuManager(this);
		cm.downloadFromExternal(force);
		cmm.downloadFromExternal(force);
		return true;
	}

	public boolean downloadEvents(boolean force) throws Exception {
		EventManager em = new EventManager(this);
		em.downloadFromExternal(force);
		return true;
	}

	public boolean downloadFeed(int feedId, boolean force) throws Exception {
		FeedItemManager fim = new FeedItemManager(this);
		fim.downloadFromExternal(feedId, false, force);
		return true;
	}

	public boolean downloadGallery(boolean force) throws Exception {
		GalleryManager gm = new GalleryManager(this);
		gm.downloadFromExternal(force);
		return true;
	}

	public boolean downloadNews(boolean force) throws Exception {
		NewsManager nm = new NewsManager(this);
		nm.downloadFromExternal(force);
		return true;
	}

	public boolean downloadOrganisations(boolean force) throws Exception {
		OrganisationManager lm = new OrganisationManager(this);
		String accessToken = PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						Const.ACCESS_TOKEN, null);

		if (accessToken == null) {
			throw new Exception("No Access Token");
		}
		try {
			lm.downloadFromExternal(force, accessToken);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
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
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
			broadcastError(getResources().getString(R.string.exception_sdcard));
			// Don't start new downloads
			isDestroyed = true;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
				scucessfull = downloadNews(force);
			}
			if ((action.equals(Const.GALLERY)) && !isDestroyed) {
				scucessfull = downloadGallery(force);
			}
			if ((action.equals(Const.FEEDS)) && !isDestroyed) {
				int feedId = intent.getExtras().getInt(Const.FEED_ID);
				scucessfull = downloadFeed(feedId, force);
			}
			if ((action.equals(Const.CAFETERIAS)) && !isDestroyed) {
				scucessfull = downloadCafeterias(force);
			}
			if ((action.equals(Const.EVENTS)) && !isDestroyed) {
				scucessfull = downloadEvents(force);
			}
			if ((action.equals(Const.ORGANISATIONS)) && !isDestroyed) {
				scucessfull = downloadOrganisations(force);
			}
		} catch (TimeoutException e) {
			if (!isDestroyed) {
				Log.e(getClass().getSimpleName(), e.getMessage());
				broadcastWarning(getResources().getString(
						R.string.exception_timeout));
			}
		} catch (IOException e) {
			if (!isDestroyed) {
				Log.e(getClass().getSimpleName(), e.getMessage());
				broadcastError(getResources().getString(
						R.string.exception_sdcard));
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(),
					"Unkown error while handling action <" + action + ">");
			Log.e(getClass().getSimpleName(), e.getMessage());
			if (!isDestroyed) {
				broadcastError(getResources().getString(
						R.string.exception_unknown));
			}
		}

		// After done the job, create an broadcast intent and send it. The
		// receivers will be informed that the download service has finished.
		if (scucessfull && !isDestroyed) {
			broadcastDownloadCompleted();
		}
	}
}