package de.tum.in.tumcampus.adapters;

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
import de.tum.in.tumcampus.fragments.DayFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class CalendarSectionsPagerAdapter extends FragmentPagerAdapter {

    private final Calendar calendar = new GregorianCalendar();
    private final Date today = new Date();
    private final boolean mWeekMode;
    private int mNumDays;

    public CalendarSectionsPagerAdapter(FragmentManager fm, boolean weekMode) {
        super(fm);
        mWeekMode = weekMode;
        mNumDays = mWeekMode?7:1;
    }

    @Override
    public int getCount() {
        calendar.setTime(today);
        calendar.add(Calendar.MONTH, -CalendarActivity.MONTH_BEFORE);
        Date firstDate = calendar.getTime();

        calendar.setTime(today);
        calendar.add(Calendar.MONTH, CalendarActivity.MONTH_AFTER);
        Date lastDate = calendar.getTime();

        return (int) ((lastDate.getTime() - firstDate.getTime()) / (DateUtils.DAY_IN_MILLIS*mNumDays));
    }

    private Date getCurrentDate(int position) {
        calendar.setTime(today);
        calendar.add(Calendar.MONTH, -CalendarActivity.MONTH_BEFORE);
        calendar.add(Calendar.DAY_OF_MONTH, position*mNumDays);
        return calendar.getTime();
    }

    @Override
    public Fragment getItem(int position) {
        return new DayFragment(getCurrentDate(position).getTime(), mNumDays);
    }

    @Override
    public long getItemId(int position) {
        return position<<3+mNumDays;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Date date = getCurrentDate(position);
        Locale l = Locale.getDefault();

        SimpleDateFormat formatDefaultStyle = new SimpleDateFormat("EEEE, dd.MM.yyy");
        String dateDefaultStyle = formatDefaultStyle.format(date);

        return dateDefaultStyle.toUpperCase(l);
    }


}