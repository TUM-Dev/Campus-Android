package de.tum.in.tumcampusapp.adapters;

import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.fragments.CafeteriaDetailsSectionFragment;
import de.tum.in.tumcampusapp.models.managers.CafeteriaMenuManager;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class CafeteriaDetailsSectionsPagerAdapter extends FragmentPagerAdapter {
	/** Current Date selected (ISO format) */
	private String date;
	private final Activity activity;
	private Cursor cursorCafeteriaDates;
	private String cafeteriaId;
	private String cafeteriaName;

	public CafeteriaDetailsSectionsPagerAdapter(Activity mainActivity, FragmentManager fm, String cafeteriaId, String cafeteriaName) {
		super(fm);
		this.activity = mainActivity;
		this.cafeteriaId = cafeteriaId;
		this.cafeteriaName = cafeteriaName;

		// default date: today or next monday if today is weekend
		if (date == null) {
			Calendar calendar = Calendar.getInstance();
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek == Calendar.SATURDAY) {
				calendar.add(Calendar.DATE, 2);
			}
			if (dayOfWeek == Calendar.SUNDAY) {
				calendar.add(Calendar.DATE, 1);
			}
			date = Utils.getDateString(calendar.getTime());
		}

		// get all (distinct) dates having menus available
		CafeteriaMenuManager cmm = new CafeteriaMenuManager(activity);
		cursorCafeteriaDates = cmm.getDatesFromDb();
		// TODO ERROR HERE
		date = cursorCafeteriaDates.getString(cursorCafeteriaDates.getColumnIndex(Const.ID_COLUMN));

		mainActivity.setTitle(cafeteriaName);

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
		args.putString(Const.DATE, date);
		args.putString(Const.CAFETERIA_ID, cafeteriaId);
		args.putString(Const.CAFETERIA_NAME, cafeteriaName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		return String.valueOf(date).toUpperCase(l);
	}

}