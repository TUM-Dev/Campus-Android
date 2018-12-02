package de.tum.in.tumcampusapp.component.ui.cafeteria.details;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;

/**
 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class CafeteriaDetailsSectionsPagerAdapter extends FragmentStatePagerAdapter {

    private int mCafeteriaId;
    private List<DateTime> dates = new ArrayList<>();

    private final CafeteriaViewModel viewModel;

    public CafeteriaDetailsSectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        TcaDb database = TcaDb.getInstance(context);
        CafeteriaLocalRepository localRepository = new CafeteriaLocalRepository(database);
        viewModel = new CafeteriaViewModel(localRepository);
    }

    @SuppressLint("CheckResult")
    public void setCafeteriaId(int cafeteriaId) {
        mCafeteriaId = cafeteriaId;
        viewModel.getAllMenuDates()
                .subscribe(dates -> {
                    this.dates = dates;
                    this.notifyDataSetChanged();
                });
    }

    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        Fragment fragment = new CafeteriaDetailsSectionFragment();
        Bundle args = new Bundle();
        args.putString(Const.DATE, DateTimeUtils.INSTANCE.getDateString(dates.get(position)));
        args.putInt(Const.CAFETERIA_ID, mCafeteriaId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        DateTimeFormatter format = DateTimeFormat.fullDate();
        DateTime date = dates.get(position);

        return format.print(date).toUpperCase(Locale.getDefault());
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

}