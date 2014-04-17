package de.tum.in.tumcampusapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.wizzard.WizNavExtrasActivity;
import de.tum.in.tumcampusapp.activities.wizzard.WizNavStartActivity;
import de.tum.in.tumcampusapp.adapters.StartSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;
import de.tum.in.tumcampusapp.services.BackgroundService;
import de.tum.in.tumcampusapp.services.ImportService;
import de.tum.in.tumcampusapp.services.SilenceService;
import de.tum.in.tumcampusapp.sidemenu.ISideNavigationCallback;
import de.tum.in.tumcampusapp.sidemenu.SideNavigationItem;
import de.tum.in.tumcampusapp.sidemenu.SideNavigationView;
import de.tum.in.tumcampusapp.sidemenu.SideNavigationView.Mode;

/**
 * Main activity displaying the categories and menu items to start each activity (feature)
 * 
 * @author Sascha Moecker
 */
public class StartActivity extends SherlockFragmentActivity implements ISideNavigationCallback {
	public static final int DEFAULT_SECTION = 1;
	public static final String LAST_CHOOSEN_SECTION = "last_choosen_section";
	public static final int REQ_CODE_COLOR_CHANGE = 0;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	StartSectionsPagerAdapter mSectionsPagerAdapter;
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private SideNavigationView sideNavigationView;

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
					Log.i(this.getClass().getSimpleName(), message);
				}
			}
			if (intent.getAction().equals(WizNavExtrasActivity.BROADCAST_NAME)) {
				Log.i(this.getClass().getSimpleName(), "Color has changed");
				StartActivity.this.shouldRestartOnResume = true;
			}
		}
	};

	boolean shouldRestartOnResume;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check if there is a result key in an intent
		if (data != null && data.hasExtra(Const.PREFS_HAVE_CHANGED) && data.getBooleanExtra(Const.PREFS_HAVE_CHANGED, false)) {
			// Restart the Activity if prefs have changed
			Intent intent = this.getIntent();
			this.finish();
			this.startActivity(intent);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_start);

		// Create the adapter that will return a fragment for each of the
		// primary sections of the app.
		this.mSectionsPagerAdapter = new StartSectionsPagerAdapter(this, this.getSupportFragmentManager());

		// Workaround for new API version. There was a security update which
		// disallows applications to execute HTTP request in the GUI main
		// thread.
		if (android.os.Build.VERSION.SDK_INT > 8) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		// Set up the ViewPager with the sections adapter.
		this.mViewPager = (ViewPager) this.findViewById(R.id.pager);
		this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
		this.mViewPager.setCurrentItem(DEFAULT_SECTION);

		// Registers receiver for download and import
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ImportService.BROADCAST_NAME);
		intentFilter.addAction(WizNavExtrasActivity.BROADCAST_NAME);
		this.registerReceiver(this.receiver, intentFilter);

		// Imports default values into database
		Intent service;
		service = new Intent(this, ImportService.class);
		service.putExtra(Const.ACTION_EXTRA, Const.DEFAULTS);
		this.startService(service);

		// Start silence Service (if already started it will just invoke a check)
		service = new Intent(this, SilenceService.class);
		this.startService(service);

		// Start daily Service (same here: if already started it will just invoke a check)
		service = new Intent(this, BackgroundService.class);
		this.startService(service);

		Boolean hideWizzardOnStartup = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Const.HIDE_WIZZARD_ON_STARTUP, false);

		// Check for important news
		// TODO: check if there are any news avaible on the to be implemented webservice - for now hide the icon in the menu_start_activity

		// Setup the side navigation
		this.sideNavigationView = (SideNavigationView) this.findViewById(R.id.side_navigation_view);
		this.sideNavigationView.setMenuItems(R.menu.menu_side);
		this.sideNavigationView.setMenuClickCallback(this);
		this.sideNavigationView.toggleMenu();
		this.sideNavigationView.setMode(Mode.LEFT);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);

		// Check the flag if user wants the wizzard to open at startup
		if (!hideWizzardOnStartup) {
			Intent intent = new Intent(this, WizNavStartActivity.class);
			this.startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getSupportMenuInflater().inflate(R.menu.menu_start_activity, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// important to unregister the broadcast receiver
		this.unregisterReceiver(this.receiver);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			// Opens the preferences screen
			Intent intent = new Intent(this, UserPreferencesActivity.class);
			this.startActivityForResult(intent, REQ_CODE_COLOR_CHANGE);
			break;

		case R.id.menu_start_news:
			// Opens the news activity
			Intent newsIntent = new Intent(this, ImportantNewsActivity.class);
			this.startActivity(newsIntent);
			break;
		case android.R.id.home:
			this.sideNavigationView.toggleMenu();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.pager_title_strip);
		if (this.shouldRestartOnResume) {
			// finish and restart myself
			this.finish();
			Intent intent = new Intent(this, this.getClass());
			this.startActivity(intent);
		}
	}

	@Override
	public void onSideNavigationItemClick(SideNavigationItem sideNavigationItem) {
		try {
			String a = this.getPackageName() + ".activities." + sideNavigationItem.getActivity();
			Class<?> clazz = Class.forName(a);
			Intent newActivity = new Intent(this.getApplicationContext(), clazz);
			this.startActivity(newActivity);
		} catch (ClassNotFoundException e) {
			Log.w("tca", "ClassNotFound", e);
		}
	}
}
