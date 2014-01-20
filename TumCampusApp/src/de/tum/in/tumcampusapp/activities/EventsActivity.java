package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.widget.Toast;
import de.tum.in.tumcampus.R;
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
	private SharedPreferences sharedPrefs;
	private EventsSectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	public EventsActivity() {
		super(Const.EVENTS, R.layout.activity_events);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPrefs.getBoolean("implicitly_id", true)==true){
		Counter();
		}
		// Request a non-forced update on startup
		super.requestDownload(false);
	}
	public void Counter()
	{
		//Counting number of the times that the user used this activity.
				SharedPreferences sp = getSharedPreferences(getString(R.string.MyPrefrences), Activity.MODE_PRIVATE);
				int myvalue = sp.getInt("events_id",0);
				myvalue=myvalue+1;
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("events_id",myvalue);
				editor.commit();
				////

				 int myIntValue = sp.getInt("events_id",0);
				 if(myIntValue==5){
						sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
						SharedPreferences.Editor editor1 = sharedPrefs.edit();
						editor1.putBoolean("events_id", true);
						editor1.commit();
						editor.putInt("events_id",0);
						editor.commit();
					 
				 Toast.makeText(this, String.valueOf(myIntValue),
							Toast.LENGTH_LONG).show();
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

		// Inits the pager adapter which handles the different fragments
		mSectionsPagerAdapter = new EventsSectionsPagerAdapter(this,
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager
				.setCurrentItem(EventsSectionsPagerAdapter.PAGE_LATESTS_EVENTS);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}
}
