package de.tum.in.tumcampusapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bugsense.trace.BugSenseHandler;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.wizzard.WizNavExtrasActivity;
import de.tum.in.tumcampusapp.activities.wizzard.WizNavStartActivity;
import de.tum.in.tumcampusapp.adapters.StartSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;
import de.tum.in.tumcampusapp.preferences.UserPreferencesActivity;
import de.tum.in.tumcampusapp.services.BackgrdService;
import de.tum.in.tumcampusapp.services.ImportService;
import de.tum.in.tumcampusapp.services.SilenceService;

/**
 * Starting point for the App.
 * 
 * @author Sascha Moecker
 */
public class StartActivity extends FragmentActivity {
	public static final int DEFAULT_SECTION = 1;
	public static final String LAST_CHOOSEN_SECTION = "last_choosen_section";
	public static final int REQ_CODE_COLOR_CHANGE = 0;
	public static final boolean TRACK_ERRORS_WITH_BUG_SENSE = true;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	StartSectionsPagerAdapter mSectionsPagerAdapter;
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	/**
	 * Receiver for Services
	 */
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ImportService.BROADCAST_NAME)) {
				String message = intent.getStringExtra(Const.MESSAGE_EXTRA);
				String action = intent.getStringExtra(Const.ACTION_EXTRA);

				if (action.length() != 0) {
					Log.i(getClass().getSimpleName(), message);
				}
			}
			if (intent.getAction().equals(WizNavExtrasActivity.BROADCAST_NAME)) {
				Log.i(getClass().getSimpleName(), "Color has changed");
				shouldRestartOnResume = true;
			}
		}
	};

	boolean shouldRestartOnResume;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check if there is a result key in an intent
		if (data != null && data.hasExtra(Const.PREFS_HAVE_CHANGED)
				&& data.getBooleanExtra(Const.PREFS_HAVE_CHANGED, false)) {
			// Restart the Activity if prefs have changed
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Init a Bug Report to https://www.bugsense.com
		if (TRACK_ERRORS_WITH_BUG_SENSE) {
			BugSenseHandler.initAndStartSession(this, "19d18764");
		}

		setContentView(R.layout.activity_start);

		// Workaround for new API version. There was a security update which
		// disallows applications to execute HTTP request in the GUI main
		// thread.
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

		}

		// Create the adapter that will return a fragment for each of the
		// primary sections of the app.
		mSectionsPagerAdapter = new StartSectionsPagerAdapter(this,
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(DEFAULT_SECTION);

		// Registers receiver for download and import
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ImportService.BROADCAST_NAME);
		intentFilter.addAction(WizNavExtrasActivity.BROADCAST_NAME);
		registerReceiver(receiver, intentFilter);

		// Imports default values into database
		Intent service;
		service = new Intent(this, ImportService.class);
		service.putExtra(Const.ACTION_EXTRA, Const.DEFAULTS);
		startService(service);

		// Imports default values into database
		service = new Intent(this, SilenceService.class);
		startService(service);

		// Start daily Service
		service = new Intent(this, BackgrdService.class);
		startService(service);

		Boolean hideWizzardOnStartup = PreferenceManager
				.getDefaultSharedPreferences(this).getBoolean(
						Const.HIDE_WIZZARD_ON_STARTUP, false);
		if (!hideWizzardOnStartup) {
			Intent intent = new Intent(this, WizNavStartActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_start_activity, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			// Opens the preferences screen
			Intent intent = new Intent(this, UserPreferencesActivity.class);
			startActivityForResult(intent, REQ_CODE_COLOR_CHANGE);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.pager_title_strip);
		if (shouldRestartOnResume) {
			// finish and restart myself
			finish();
			Intent intent = new Intent(this, this.getClass());
			startActivity(intent);
		}
	}
}
