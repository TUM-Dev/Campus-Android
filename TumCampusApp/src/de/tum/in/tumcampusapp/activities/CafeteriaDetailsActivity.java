package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.CafeteriaDetailsSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;

public class CafeteriaDetailsActivity extends FragmentActivity {

	private CafeteriaDetailsSectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private String cafeteriaId;
	private String cafeteriaName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cafeteriadetails);

		cafeteriaId = getIntent().getExtras().getString(Const.CAFETERIA_ID);
		cafeteriaName = getIntent().getExtras().getString(Const.CAFETERIA_NAME);

		// Create the adapter that will return a fragment for each of the
		// primary sections of the app.
		mSectionsPagerAdapter = new CafeteriaDetailsSectionsPagerAdapter(this, getSupportFragmentManager(), cafeteriaId, cafeteriaName);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}
}
