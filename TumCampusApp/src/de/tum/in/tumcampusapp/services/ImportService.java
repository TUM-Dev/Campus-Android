package de.tum.in.tumcampusapp.services;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.Feed;
import de.tum.in.tumcampusapp.models.Location;
import de.tum.in.tumcampusapp.models.managers.FeedManager;
import de.tum.in.tumcampusapp.models.managers.LectureItemManager;
import de.tum.in.tumcampusapp.models.managers.LectureManager;
import de.tum.in.tumcampusapp.models.managers.LocationManager;

/**
 * Service used to import files from internal sd-card
 */
public class ImportService extends IntentService {
	/**
	 * Import broadcast identifier
	 */
	public final static String BROADCAST_NAME = "de.tum.in.newtumcampus.intent.action.BROADCAST_IMPORT";
	public static final String CSV_FEEDS = "feeds.csv";
	public static final String CSV_LOCATIONS = "locations.csv";
	public static final String IMPORT_SERVICE = "ImportService";
	public static final String ISO = "ISO-8859-1";

	/**
	 * default init (run intent in new thread)
	 */
	public ImportService() {
		super(IMPORT_SERVICE);
	}

	/**
	 * Import default feeds from assets
	 * 
	 * @throws Exception
	 */
	public void importFeedsDefaults() throws Exception {

		FeedManager nm = new FeedManager(this);
		if (nm.empty()) {
			List<String[]> rows = Utils.readCsv(getAssets().open(CSV_FEEDS),
					ISO);

			for (String[] row : rows) {
				nm.insertUpdateIntoDb(new Feed(row[0], row[1]));
			}
		}
	}

	/**
	 * imports lecture items from TUMOnline HINT: access token have to be set
	 * 
	 * @author Daniel G. Mayr
	 */
	public void importLectureItemsFromTUMOnline(boolean force) throws Exception {
		LectureItemManager lim = new LectureItemManager(this);
		lim.importFromTUMOnline(this, force);

		LectureManager lm = new LectureManager(this);
		lm.updateLectures();
	}

	/**
	 * Import default location and opening hours from assets
	 * 
	 * <pre>
	 * @param force boolean force import of locations
	 * @throws Exception
	 * </pre>
	 */
	public void importLocationsDefaults(boolean force) throws Exception {

		LocationManager lm = new LocationManager(this);
		if (lm.empty() || force) {
			List<String[]> rows = Utils.readCsv(
					getAssets().open(CSV_LOCATIONS), ISO);

			for (String[] row : rows) {
				lm.replaceIntoDb(new Location(Integer.parseInt(row[0]), row[1],
						row[2], row[3], row[4], row[5], row[6], row[7], row[8]));
			}
		}
	}

	/**
	 * Send notification message to service caller
	 * 
	 * <pre>
	 * @param e Exception, get message and stacktrace from 
	 * @param info Notification info, append to exception message
	 * </pre>
	 */
	public void message(Exception e, String info) {
		Utils.log(e, info);

		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));

		String message = e.getMessage();
		message(info + " " + message, getString(R.string.error));
	}

	/**
	 * Send notification message to service caller
	 * 
	 * <pre>
	 * @param message Notification message
	 * @param action Notification action (e.g. error, completed)
	 * </pre>
	 */
	public void message(String message, String action) {
		Intent intentSend = new Intent();
		intentSend.setAction(BROADCAST_NAME);
		intentSend.putExtra(Const.MESSAGE_EXTRA, message);
		intentSend.putExtra(Const.ACTION_EXTRA, action);
		sendBroadcast(intentSend);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.log("ImportService has started");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.log("ImportService has stopped");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getStringExtra(Const.ACTION_EXTRA);
		Utils.log(action);

		// import all defaults or only one action
		if (action.equals(Const.DEFAULTS)) {
			try {
				message("Starting to imported assets", "");
				// get current app version
				int version = getPackageManager().getPackageInfo(
						this.getPackageName(), 0).versionCode;

				// check if database update is needed
				boolean update = false;
				File f = new File(getFilesDir() + "/" + version);
				if (!f.exists()) {
					updateDatabase();
					update = true;
				}
				importLocationsDefaults(update);
				importFeedsDefaults();
				f.createNewFile();
				message("Successfully imported Assets", Const.ACTION_EXTRA);
			} catch (Exception e) {
				// TODO Give better feedback
				Utils.log(e, "");
			}
		} else if (action.equals(Const.LECTURES_TUM_ONLINE)) {
			boolean force = intent.getBooleanExtra(Const.FORCE_DOWNLOAD, false);
			try {
				message("Starting to imported lecture schedule from TUMOnline",
						Const.LECTURES_TUM_ONLINE_START);
				importLectureItemsFromTUMOnline(force);
				message("Successfully imported lectures schedule from TUMOnline",
						Const.LECTURES_TUM_ONLINE_FINISH);
			} catch (Exception e) {
				// TODO Give better feedback
				Utils.log(e, "");
			}
		}
	}

	private void updateDatabase() {
		// TODO Whatfor?
	}
}