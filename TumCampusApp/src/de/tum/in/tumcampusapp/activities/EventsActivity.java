package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.adapters.EventsSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;

/**
 * Displays Events which are fetched from Facebook
 * 
 * @author Sascha Moecker
 * 
 */
public class EventsActivity extends ActivityForDownloadingExternal {

	private EventsSectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	public EventsActivity() {
		super(Const.EVENTS, R.layout.activity_events);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Request a non-forced update on startup
		super.requestDownload(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.pager_title_strip);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Inits the pager adapter which handles the different fragments
		mSectionsPagerAdapter = new EventsSectionsPagerAdapter(this,
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager
				.setCurrentItem(EventsSectionsPagerAdapter.PAGE_LATESTS_EVENTS);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}
}
