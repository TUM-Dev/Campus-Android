package de.tum.in.tumcampusapp.component.ui.cafeteria.details;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class CafeteriaDetailsSectionsPagerAdapter extends FragmentStatePagerAdapter {

    private int cafeteriaId;
    private List<DateTime> dates;
    private DateTimeFormatter formatter;

    public CafeteriaDetailsSectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        formatter = DateTimeFormat.fullDate();
    }

    public void setCafeteriaId(int cafeteriaId) {
        this.cafeteriaId = cafeteriaId;
    }

    public void update(List<DateTime> menuDates) {
        dates = menuDates;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public Fragment getItem(int position) {
        DateTime dateTime = dates.get(position);
        return CafeteriaDetailsSectionFragment.newInstance(cafeteriaId, dateTime);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        DateTime date = dates.get(position);
        return formatter.print(date).toUpperCase(Locale.getDefault());
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

}
