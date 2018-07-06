package de.tum.in.tumcampusapp.component.ui.cafeteria.details;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class CafeteriaDetailsSectionsPagerAdapter extends FragmentStatePagerAdapter {
    private int mCafeteriaId;

    private List<DateTime> dates = new ArrayList<>();

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    public CafeteriaDetailsSectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setCafeteriaId(Context context, int cafeteriaId) {
        mCafeteriaId = cafeteriaId;
        CafeteriaRemoteRepository remoteRepository = CafeteriaRemoteRepository.INSTANCE;
        remoteRepository.setTumCabeClient(TUMCabeClient.getInstance(context));
        CafeteriaLocalRepository localRepository = CafeteriaLocalRepository.INSTANCE;
        localRepository.setDb(TcaDb.getInstance(context));
        CafeteriaViewModel cafeteriaViewModel = new CafeteriaViewModel(localRepository, remoteRepository, mDisposable);
        Disposable getAllMenuDates = cafeteriaViewModel.getAllMenuDates()
                .subscribe(dates -> {
                    this.dates = dates;
                    this.notifyDataSetChanged();
                });
        mDisposable.add(getAllMenuDates);
        // Tell we just update the data

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