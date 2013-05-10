package de.tum.in.tumcampusapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.StartSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.preferences.UserPreferencesActivity;
import de.tum.in.tumcampusapp.services.ImportService;

public class StartActivity extends FragmentActivity {

	public static final int DEFAULT_SECTION = 1;
	public static final String LAST_CHOOSEN_SECTION = "last_choosen_section";

	ImageView mImageView;

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
			if (intent.getAction().equals(ImportService.broadcast)) {
				String message = intent.getStringExtra(Const.MESSAGE_EXTRA);
				String action = intent.getStringExtra(Const.ACTION_EXTRA);

				if (action.length() != 0) {
					Log.i(getClass().getSimpleName(), message);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		mImageView = (ImageView) findViewById(R.id.activity_start_loading_image);
		mImageView.setVisibility(View.VISIBLE);
		// Workaround for new API version. There was a security update which
		// disallows applications to execute HTTP request in the GUI main
		// thread.
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		// Create the adapter that will return a fragment for each of the
		// primary sections of the app.
		mSectionsPagerAdapter = new StartSectionsPagerAdapter(this, getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(DEFAULT_SECTION);

		// Registers receiver for download and import
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ImportService.broadcast);
		registerReceiver(receiver, intentFilter);

		// Imports default values into database
		Intent service = new Intent(this, ImportService.class);
		service.putExtra(Const.ACTION_EXTRA, Const.DEFAULTS);
		startService(service);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mImageView.setVisibility(View.GONE);
	}
}
