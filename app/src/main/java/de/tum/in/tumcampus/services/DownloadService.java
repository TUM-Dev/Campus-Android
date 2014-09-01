package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CafeteriaManager;
import de.tum.in.tumcampus.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.EventManager;
import de.tum.in.tumcampus.models.managers.FeedItemManager;
import de.tum.in.tumcampus.models.managers.GalleryManager;
import de.tum.in.tumcampus.models.managers.NewsManager;
import de.tum.in.tumcampus.models.managers.OrganisationManager;
import de.tum.in.tumcampus.models.managers.SyncManager;

/**
 * Service used to download files from external pages
 */
public class DownloadService extends IntentService {

	/**
	 * Download broadcast identifier
	 */
	public final static String BROADCAST_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_DOWNLOAD";
	private static final String DOWNLOAD_SERVICE = "DownloadService";
    public static final String UPDATE_PREFS = "update";
    public static final String LAST_UPDATE = "last_update";

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

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.log("DownloadService service has started");

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
        Utils.log("DownloadService service has stopped");
        isDestroyed = true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean successful = false;
        String action = intent.getStringExtra(Const.ACTION_EXTRA);
        boolean force = intent.getBooleanExtra(Const.FORCE_DOWNLOAD, false);
        boolean launch = intent.getBooleanExtra(Const.APP_LAUNCHES, false);

        if (action == null) {
            // No action: leave service
            return;
        }

        // Check if device has a internet connection
        if(Utils.isConnected(this) && (launch || !Utils.isConnectedMobileData(this))) {

            Log.i(getClass().getSimpleName(), "Handle action <" + action + ">");

            try {
                if ((action.equals(Const.DOWNLOAD_ALL_FROM_EXTERNAL)) && !isDestroyed) {

                    // TODO Implement downloading each type of external source
                    // TODO Also download my tum stuff

                    // downloadGallery(force);
                    // downloadNews(force);
                    downloadCafeterias(force);
                    // downloadEvents(force);
                    downloadOrganisations(force);

                    successful = true;
                }
                if ((action.equals(Const.NEWS)) && !isDestroyed) {
                    successful = downloadNews(force);
                }
                if ((action.equals(Const.GALLERY)) && !isDestroyed) {
                    successful = downloadGallery(force);
                }
                if ((action.equals(Const.FEEDS)) && !isDestroyed) {
                    int feedId = intent.getExtras().getInt(Const.FEED_ID);
                    successful = downloadFeed(feedId, force);
                }
                if ((action.equals(Const.CAFETERIAS)) && !isDestroyed) {
                    successful = downloadCafeterias(force);
                }
                if ((action.equals(Const.EVENTS)) && !isDestroyed) {
                    successful = downloadEvents(force);
                }
                if ((action.equals(Const.ORGANISATIONS)) && !isDestroyed) {
                    successful = downloadOrganisations(force);
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
                if (!isDestroyed) {
                    broadcastError(getResources().getString(
                            R.string.exception_unknown));
                }
            }
        }

        if ((action.equals(Const.DOWNLOAD_ALL_FROM_EXTERNAL)) && !isDestroyed) {
            if (successful) {
                SharedPreferences prefs = getSharedPreferences(UPDATE_PREFS, 0);
                prefs.edit().putLong(LAST_UPDATE, System.currentTimeMillis()).apply();
            }
            CardManager.update(this);
            successful = true;
        }

        // After done the job, create an broadcast intent and send it. The
        // receivers will be informed that the download service has finished.
        if (successful && !isDestroyed) {
            broadcastDownloadCompleted();
        }
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
		OrganisationManager om = new OrganisationManager(this);
		String accessToken = PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						Const.ACCESS_TOKEN, null);

		if (accessToken == null) {
			throw new Exception("No Access Token");
		}
		try {
			om.downloadFromExternal(force, accessToken);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
		}
		return true;
	}

    /**
     * Gets the time when BackgroundService was called last time
     * @param c Context
     * @return time when BackgroundService was executed last time
     * */
    public static long lastUpdate(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(UPDATE_PREFS, 0);
        return prefs.getLong(LAST_UPDATE, 0);
    }
}