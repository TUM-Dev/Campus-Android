package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.Location;
import de.tum.in.tumcampus.models.managers.CafeteriaManager;
import de.tum.in.tumcampus.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.LectureItemManager;
import de.tum.in.tumcampus.models.managers.LectureManager;
import de.tum.in.tumcampus.models.managers.LocationManager;
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
    public static final String LAST_UPDATE = "last_update";
    public static final String CSV_LOCATIONS = "locations.csv";
    public static final String ISO = "ISO-8859-1";
    private static final String LOCATIONS_VERSION = "locations_version";

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

                    downloadNews(force);
                    downloadCafeterias(force);
                    downloadOrganisations(force);
                    importLocationsDefaults();

                    successful = true;
                }
                if ((action.equals(Const.NEWS)) && !isDestroyed) {
                    successful = downloadNews(force);
                }
                if ((action.equals(Const.LECTURES_TUM_ONLINE)) && !isDestroyed) {
                    successful = importLectureItemsFromTUMOnline(force);
                }
                if ((action.equals(Const.CAFETERIAS)) && !isDestroyed) {
                    successful = downloadCafeterias(force);
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
                SharedPreferences prefs = getSharedPreferences(Const.INTERNAL_PREFS, 0);
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

        // Do all other import stuff that is not relevant for creating the viewing the start page
        if ((action.equals(Const.DOWNLOAD_ALL_FROM_EXTERNAL)) && !isDestroyed) {
            try {
                importLectureItemsFromTUMOnline(force);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
     * imports lecture items from TUMOnline HINT: access token has to be set
     */
    public boolean importLectureItemsFromTUMOnline(boolean force) throws Exception {
        LectureItemManager lim = new LectureItemManager(this);
        lim.importFromTUMOnline(this, force);

        LectureManager lm = new LectureManager(this);
        lm.updateLectures();
        return true;
    }

    /**
     * Import default location and opening hours from assets
     */
    public void importLocationsDefaults() throws Exception {
        // get current app version
        int version = getPackageManager().getPackageInfo(
                this.getPackageName(), 0).versionCode;

        // check if database update is needed
        SharedPreferences prefs = getSharedPreferences(Const.INTERNAL_PREFS, 0);
        boolean update = prefs.getInt(LOCATIONS_VERSION, -1)!=version;

        LocationManager lm = new LocationManager(this);
        if (lm.empty() || update) {
            List<String[]> rows = Utils.readCsv(getAssets().open(CSV_LOCATIONS), ISO);

            for (String[] row : rows) {
                lm.replaceIntoDb(new Location(Integer.parseInt(row[0]), row[1],
                        row[2], row[3], row[4], row[5], row[6], row[7], row[8]));
            }
        }
        prefs.edit().putInt(LOCATIONS_VERSION,version).apply();
    }

    /**
     * Gets the time when BackgroundService was called last time
     * @param c Context
     * @return time when BackgroundService was executed last time
     * */
    public static long lastUpdate(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(Const.INTERNAL_PREFS, 0);
        return prefs.getLong(LAST_UPDATE, 0);
    }
}