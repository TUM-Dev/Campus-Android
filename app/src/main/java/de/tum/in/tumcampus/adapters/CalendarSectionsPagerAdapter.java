package de.tum.in.tumcampus.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.tum.in.tumcampus.activities.CalendarActivity;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.fragments.CalendarSectionFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class CalendarSectionsPagerAdapter extends FragmentPagerAdapter {

    private final Calendar calendar = new GregorianCalendar();
    private final Date today = new Date();

    public CalendarSectionsPagerAdapter(FragmentManager fm) {
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

        return (int) ((lastDate.getTime() - firstDate.getTime()) / DateUtils.DAY_IN_MILLIS);
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

        args.putString("date", Utils.getDateTimeString(getCurrentDate(position)));

        fragment.setArguments(args);
        return fragment;
    }

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


}