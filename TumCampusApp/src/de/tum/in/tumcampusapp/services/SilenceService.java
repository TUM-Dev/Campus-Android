package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.LectureItemManager;

/** Service used to silence the mobile during lectures */
public class SilenceService extends IntentService {

	/**
	 * Interval in milliseconds to check for current lectures
	 */
	public static int CHECK_INTERVAL = 60000 * 15; // 15 Minutes
	public static final String SILENCE_SERVICE = "SilenceService";

	/**
	 * default init (run intent in new thread)
	 */
	public SilenceService() {
		super(SILENCE_SERVICE);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.log(""); // log create
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.log(""); // log destroy
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		// loop until silence mode gets disabled in settings
		while (Utils.getSettingBool(this, Const.SILENCE_SERVICE)) {
			AudioManager am;
			
			am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

			LectureItemManager lim = new LectureItemManager(this);
			if (!lim.hasLectures()) {
				// no lectures available
				return;
			}
			
			Cursor c = lim.getCurrentFromDb();
			if (c.getCount() != 0) {
				// if current lecture(s) found, silence the mobile
				Utils.setSettingBool(this, Const.SILENCE_ON, true);

				Utils.log("set ringer mode: silent");
				am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			} else if (Utils.getSettingBool(this, Const.SILENCE_ON)) {
				// default: no silence
				Utils.log("set ringer mode: normal");
				am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				Utils.setSettingBool(this, Const.SILENCE_ON, false);
			}

			// wait until next check
			synchronized (this) {
				try {
					wait(CHECK_INTERVAL);
				} catch (Exception e) {
					Utils.log(e, "");
				}
			}
		}
	}
}