package de.tum.in.tumcampusapp.adapters;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.tum.in.tumcampusapp.fragments.SearchFragment;

/**
 * A simple pager adapter that represents PageFragment objects, in sequence.
 */
@SuppressLint("UseSparseArrays")
public class SearchPagerAdapter extends FragmentPagerAdapter {
	public static final int NUMBER_OF_PAGES = 3;
	public static final int TAB_ROOMS = 0;
	public static final int TAB_PERSONS = 1;
	public static final int TAB_LVS = 2;
	public static final String argPos = "extraPosition";

	public List<SearchFragment> pages;

	public SearchPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
		this.pages = new ArrayList<SearchFragment>();

		for (int i = 0; i < NUMBER_OF_PAGES; i++) {
			SearchFragment fragment = new SearchFragment();

			// Some args
			Bundle bdl = new Bundle(1);
			bdl.putInt(argPos, i);
			fragment.setArguments(bdl);

			// Save the reference
			this.pages.add(fragment);
		}
	}

	@Override
	public int getCount() {
		return NUMBER_OF_PAGES;
	}

	@Override
	public Fragment getItem(int position) {
		// pull from map
		return this.pages.get(position);

	}

}