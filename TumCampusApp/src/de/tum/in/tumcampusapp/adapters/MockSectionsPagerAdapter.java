package de.tum.in.tumcampusapp.adapters;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.tum.in.tumcampusapp.fragments.MockSectionFragment;

public class MockSectionsPagerAdapter extends FragmentPagerAdapter {
	public final static int PAGE_COUNT = 2;
	private final Activity activity;

	public MockSectionsPagerAdapter(Activity mainActivity, FragmentManager fm) {
		super(fm);
		this.activity = mainActivity;
	}

	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment = new MockSectionFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return "0";
		case 1:
			return "1";
		}
		return "";
	}
}