package de.tum.in.tumcampusapp.adapters;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.fragments.CafeteriaDetailsSectionFragment;
import de.tum.in.tumcampusapp.models.managers.CafeteriaMenuManager;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class CafeteriaDetailsSectionsPagerAdapter extends FragmentPagerAdapter {
	/** Current Date selected (ISO format) */
	private ArrayList<String> dates = new ArrayList<String>();
	private final Activity activity;
	private Cursor cursorCafeteriaDates;
	private String cafeteriaId;
	private String cafeteriaName;

	@SuppressWarnings("deprecation")
	public CafeteriaDetailsSectionsPagerAdapter(Activity mainActivity, FragmentManager fm, String cafeteriaId, String cafeteriaName) {
		super(fm);
		this.activity = mainActivity;
		this.cafeteriaId = cafeteriaId;
		this.cafeteriaName = cafeteriaName;

		// get all (distinct) dates having menus available
		CafeteriaMenuManager cmm = new CafeteriaMenuManager(activity);
		cursorCafeteriaDates = cmm.getDatesFromDb();
		activity.startManagingCursor(cursorCafeteriaDates);

		mainActivity.setTitle(cafeteriaName);

		for (int position = 0; position < getCount(); position++) {
			cursorCafeteriaDates.moveToPosition(position);
			dates.add(cursorCafeteriaDates.getString(cursorCafeteriaDates.getColumnIndex(Const.ID_COLUMN)));
		}

		// reset new items counter
		CafeteriaMenuManager.lastInserted = 0;
	}

	@Override
	public int getCount() {
		return cursorCafeteriaDates.getCount();
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		Fragment fragment = new CafeteriaDetailsSectionFragment();
		Bundle args = new Bundle();
		args.putString(Const.DATE, dates.get(position));
		args.putString(Const.CAFETERIA_ID, cafeteriaId);
		args.putString(Const.CAFETERIA_NAME, cafeteriaName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		return String.valueOf(dates.get(position)).toUpperCase(l);
	}
}