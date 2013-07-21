package de.tum.in.tumcampusapp.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.wizzard.WizNavStartActivity;
import de.tum.in.tumcampusapp.auxiliary.AccessTokenManager;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.CafeteriaManager;
import de.tum.in.tumcampusapp.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampusapp.models.managers.EventManager;
import de.tum.in.tumcampusapp.models.managers.FeedItemManager;
import de.tum.in.tumcampusapp.models.managers.GalleryManager;
import de.tum.in.tumcampusapp.models.managers.NewsManager;
import de.tum.in.tumcampusapp.models.managers.SyncManager;

public class UserPreferencesActivity extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private AccessTokenManager accessTokenManager = new AccessTokenManager(this);
	private Context context = this;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		// Click listener for preference list entries. Used to simulate a button
		// (since it is not possible to add a button to the preferences screen)
		Preference buttonToken = (Preference) findPreference("button_token");
		buttonToken
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						// Querys for a access token from TUMOnline
						accessTokenManager.setupAccessToken();
						return true;
					}
				});
		Preference buttonWizzard = (Preference) findPreference("button_wizzard");
		buttonWizzard
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						finish();
						Intent intent = new Intent(
								UserPreferencesActivity.this,
								WizNavStartActivity.class);
						startActivity(intent);
						return true;
					}
				});
		Preference buttonClearCache = (Preference) findPreference("button_clear_cache");
		buttonClearCache
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {

						try {
							Utils.getCacheDir("");
						} catch (Exception e) {
							Toast.makeText(context, R.string.exception_sdcard,
									Toast.LENGTH_SHORT).show();
							return true;
						}

						CafeteriaManager cm = new CafeteriaManager(context);
						cm.removeCache();

						CafeteriaMenuManager cmm = new CafeteriaMenuManager(
								context);
						cmm.removeCache();

						EventManager em = new EventManager(context);
						em.removeCache();

						FeedItemManager fim = new FeedItemManager(context);
						fim.removeCache();

						GalleryManager gm = new GalleryManager(context);
						gm.removeCache();

						NewsManager nm = new NewsManager(context);
						nm.removeCache();

						// table of all download events
						SyncManager sm = new SyncManager(context);
						sm.deleteFromDb();

						Toast.makeText(context, R.string.success_clear_cache,
								Toast.LENGTH_SHORT).show();

						return true;
					}
				});

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// if the color scheme has changed, fire a on activity result intent
		// which can be used by the calling activity
		if (key.equals("color_scheme")) {
			Intent returnIntent = new Intent();
			returnIntent.putExtra(Const.PREFS_HAVE_CHANGED, true);
			setResult(RESULT_OK, returnIntent);
		}
	}
}