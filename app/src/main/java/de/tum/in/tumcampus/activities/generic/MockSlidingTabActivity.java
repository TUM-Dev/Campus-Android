package de.tum.in.tumcampus.activities.generic;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.adapters.MockSectionsPagerAdapter;

/**
 * Mock Activity to demonstrate the basic fragment based navigation using tabs.
 * 
 * @author Sascha Moecker
 * 
 */
public class MockSlidingTabActivity extends ActionBarActivity {

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
}
