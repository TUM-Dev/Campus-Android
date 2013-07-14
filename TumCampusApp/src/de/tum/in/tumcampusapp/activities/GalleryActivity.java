package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.adapters.GallerySectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;

public class GalleryActivity extends ActivityForDownloadingExternal {

	private GallerySectionsPagerAdapter mSectionsPagerAdapter;

	private ViewPager mViewPager;

	public GalleryActivity() {
		super(Const.GALLERY, R.layout.activity_gallery);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		mSectionsPagerAdapter = new GallerySectionsPagerAdapter(this,
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager
				.setCurrentItem(GallerySectionsPagerAdapter.PAGE_LATESTS_GALLERY);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}
}
