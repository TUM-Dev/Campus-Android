package de.tum.in.tumcampusapp.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.fragments.GallerySectionFragment;

public class GallerySectionsPagerAdapter extends FragmentPagerAdapter {
	public final static String ARG_GALLERY_MODE = "event_mode";
	public final static int PAGE_COUNT = 2;
	public final static int PAGE_LATESTS_GALLERY = 0;
	public final static int PAGE_PAST_GALLERY = 1;

	private final Activity activity;

	public GallerySectionsPagerAdapter(Activity mainActivity, FragmentManager fm) {
		super(fm);
		this.activity = mainActivity;
	}

	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment = new GallerySectionFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_GALLERY_MODE, position);
		fragment.setArguments(args);
		return fragment;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case PAGE_LATESTS_GALLERY:
			return activity.getResources().getString(R.string.latest_gallery)
					.toUpperCase();
		case PAGE_PAST_GALLERY:
			return activity.getResources().getString(R.string.past_gallery)
					.toUpperCase();
		}
		return "";
	}
}