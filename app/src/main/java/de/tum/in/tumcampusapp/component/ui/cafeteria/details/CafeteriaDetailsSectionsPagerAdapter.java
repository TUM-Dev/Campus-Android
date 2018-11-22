package de.tum.in.tumcampusapp.component.ui.cafeteria.details;

import android.annotation.SuppressLint;
import android.content.Context;

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
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository;
import de.tum.in.tumcampusapp.database.TcaDb;

/**
 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class CafeteriaDetailsSectionsPagerAdapter extends FragmentStatePagerAdapter {

    private DateTimeFormatter formatter;
    private int mCafeteriaId;

    private List<DateTime> dates = new ArrayList<>();

    public CafeteriaDetailsSectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        formatter = DateTimeFormat.fullDate();
    }

    @SuppressLint("CheckResult")
    public void setCafeteriaId(Context context, int cafeteriaId) {
        mCafeteriaId = cafeteriaId;

        TUMCabeClient client = TUMCabeClient.getInstance(context);
        CafeteriaRemoteRepository remoteRepository = new CafeteriaRemoteRepository(client);

        TcaDb database = TcaDb.getInstance(context);
        CafeteriaLocalRepository localRepository = new CafeteriaLocalRepository(database);

        CafeteriaViewModel cafeteriaViewModel = new CafeteriaViewModel(localRepository, remoteRepository);

        // TODO
        cafeteriaViewModel
                .getAllMenuDates()
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
        DateTime dateTime = dates.get(position);
        return CafeteriaDetailsSectionFragment.newInstance(mCafeteriaId, dateTime);
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