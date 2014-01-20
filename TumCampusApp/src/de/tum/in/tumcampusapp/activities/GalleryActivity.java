package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.widget.Toast;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
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

	public GalleryActivity() {
		super(Const.GALLERY, R.layout.activity_gallery);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPrefs.getBoolean("implicitly_id", true)==true){
		Counter();
		}
		super.requestDownload(false);
	}
	public void Counter()
	{
		//Counting number of the times that the user used this activity.
				SharedPreferences sp = getSharedPreferences(getString(R.string.MyPrefrences), Activity.MODE_PRIVATE);
				int myvalue = sp.getInt("gallery_id",0);
				myvalue=myvalue+1;
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("gallery_id",myvalue);
				editor.commit();
				

				 int myIntValue = sp.getInt("gallery_id",0);
				 
				 Toast.makeText(this, String.valueOf(myIntValue),
							Toast.LENGTH_LONG).show();
				 if(myIntValue==5){
						sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
						SharedPreferences.Editor editor1 = sharedPrefs.edit();
						editor1.putBoolean("gallery_id", true);
						editor1.commit();
						editor.putInt("gallery_id",0);
						editor.commit();
			
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
		mSectionsPagerAdapter = new GallerySectionsPagerAdapter(this,
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager
				.setCurrentItem(GallerySectionsPagerAdapter.PAGE_LATESTS_GALLERY);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}
}
