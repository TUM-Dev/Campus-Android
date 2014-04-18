package de.tum.in.tumcampusapp.activities.generic;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.MockSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;

/**
 * Mock Activity to demonstrate the basic fragment based navigation using tabs.
 * 
 * @author Sascha Moecker
 * 
 */
public class MockSlidingTabActivity extends SherlockFragmentActivity {

	private MockSectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mocksslidingtab);

		mSectionsPagerAdapter = new MockSectionsPagerAdapter(this,
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.pager_title_strip);
	}
}
