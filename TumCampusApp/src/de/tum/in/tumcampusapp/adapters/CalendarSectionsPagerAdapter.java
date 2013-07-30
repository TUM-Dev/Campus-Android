package de.tum.in.tumcampusapp.adapters;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.tum.in.tumcampusapp.activities.CalendarActivity;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.fragments.CalendarSectionFragment;

public class CalendarSectionsPagerAdapter extends FragmentPagerAdapter {
	public final static int PAGE_COUNT = 5;

	Calendar calendar = new GregorianCalendar();
	Date today = new Date();

	public CalendarSectionsPagerAdapter(Activity mainActivity,
			FragmentManager fm) {
		super(fm);
	}

	@Override
	public int getCount() {
		calendar.setTime(today);
		calendar.add(Calendar.MONTH, -CalendarActivity.MONTH_BEFORE);
		Date firstDate = calendar.getTime();

		calendar.setTime(today);
		calendar.add(Calendar.MONTH, CalendarActivity.MONTH_AFTER);
		Date lastDate = calendar.getTime();

		long days = (long) (lastDate.getTime() - firstDate.getTime())
				/ (1000 * 60 * 60 * 24);

		return (int) days;
	}

	private String getCurrentDateAsString(int position) {
		calendar.setTime(today);
		calendar.add(Calendar.MONTH, -CalendarActivity.MONTH_BEFORE);
		calendar.add(Calendar.DAY_OF_MONTH, position);
		Date date = calendar.getTime();
		return Utils.getDateTimeString(date);
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment = new CalendarSectionFragment();
		Bundle args = new Bundle();

		args.putString("date", getCurrentDateAsString(position));

		fragment.setArguments(args);
		return fragment;
	}

	@SuppressWarnings("deprecation")
	@Override
	public CharSequence getPageTitle(int position) {
		return getCurrentDateAsString(position).substring(0, 10);
	}
}