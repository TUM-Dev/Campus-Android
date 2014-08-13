package de.tum.in.tumcampus.adapters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.tum.in.tumcampus.activities.CalendarActivity;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.fragments.CalendarSectionFragment;

public class CalendarSectionsPagerAdapter extends FragmentPagerAdapter {
	public final static int PAGE_COUNT = 0;

	Calendar calendar = new GregorianCalendar();
	Date today = new Date();
	boolean updateMode;

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

		long days = (lastDate.getTime() - firstDate.getTime())
				/ (1000 * 60 * 60 * 24);

		if (updateMode) {
			return PAGE_COUNT;
		} else {
			return (int) days;
		}
	}

	private Date getCurrentDate(int position) {
		calendar.setTime(today);
		calendar.add(Calendar.MONTH, -CalendarActivity.MONTH_BEFORE);
		calendar.add(Calendar.DAY_OF_MONTH, position);
		return calendar.getTime();
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment = new CalendarSectionFragment();
		Bundle args = new Bundle();

		args.putString("date",
				Utils.getDateTimeString(getCurrentDate(position)));
		args.putBoolean("update_mode", updateMode);

		fragment.setArguments(args);
		return fragment;
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public CharSequence getPageTitle(int position) {
		Date date = getCurrentDate(position);
		Locale l = Locale.getDefault();

		// return getCurrentDate(position).toLocaleString().subSequence(0, 10);
		SimpleDateFormat formatEE = new SimpleDateFormat("EEEE");
		String finalDay = formatEE.format(date);

		SimpleDateFormat formatDefaultStyle = new SimpleDateFormat("dd.MM.yyy");
		String dateDefaultStyle = formatDefaultStyle.format(date);

		String pageTitleToShow = finalDay + ", " + dateDefaultStyle;

		return (pageTitleToShow).toUpperCase(l);
	}

	public void setUpdateMode(boolean updateMode) {
		this.updateMode = updateMode;
	}
}