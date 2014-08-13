package de.tum.in.tumcampus.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.fragments.EventsSectionFragment;

public class EventsSectionsPagerAdapter extends FragmentPagerAdapter {
	public final static String ARG_EVENT_MODE = "event_mode";
	public final static int PAGE_COUNT = 2;
	public final static int PAGE_LATESTS_EVENTS = 0;
	public final static int PAGE_PAST_EVENTS = 1;

	private final Activity activity;

	public EventsSectionsPagerAdapter(Activity mainActivity, FragmentManager fm) {
		super(fm);
		this.activity = mainActivity;
	}

	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment = new EventsSectionFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_EVENT_MODE, position);
		fragment.setArguments(args);
		return fragment;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case PAGE_LATESTS_EVENTS:
			return activity.getResources().getString(R.string.latest_events)
					.toUpperCase();
		case PAGE_PAST_EVENTS:
			return activity.getResources().getString(R.string.past_events)
					.toUpperCase();
		}
		return "";
	}
}