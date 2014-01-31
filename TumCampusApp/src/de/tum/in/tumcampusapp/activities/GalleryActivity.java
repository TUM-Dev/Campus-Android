package de.tum.in.tumcampusapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.activities.ImplicitCounter;
import de.tum.in.tumcampusapp.adapters.GallerySectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;

/**
 * Displays images fetched from Facebook.
 * 
 * @review Sascha Moecker
 * 
 */
public class GalleryActivity extends ActivityForDownloadingExternal {

	private GallerySectionsPagerAdapter mSectionsPagerAdapter;
	private SharedPreferences sharedPrefs;
	private ViewPager mViewPager;
	
	//private ImplicitCounter implicitCounter;


	public GalleryActivity() {
		super(Const.GALLERY, R.layout.activity_gallery);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.requestDownload(false);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPrefs.getBoolean("implicitly_id", true)){
			ImplicitCounter.Counter("gallery_id",getApplicationContext());
		}
		
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.pager_title_strip);
	}
	@Override
	protected void onStart() {
		super.onStart();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		mSectionsPagerAdapter = new GallerySectionsPagerAdapter(this,
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager
				.setCurrentItem(GallerySectionsPagerAdapter.PAGE_LATESTS_GALLERY);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}
}
