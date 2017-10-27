package de.tum.in.tumcampusapp.adapters;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.fragments.CafeteriaDetailsSectionFragment;
import de.tum.in.tumcampusapp.managers.CafeteriaMenuManager;

/**
 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class CafeteriaDetailsSectionsPagerAdapter extends FragmentStatePagerAdapter {
    private int mCafeteriaId;
    private Cursor cursorCafeteriaDates;

    /**
     * Current Date selected (ISO format)
     */
    private final List<String> dates = new ArrayList<>();

    public CafeteriaDetailsSectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @SuppressWarnings("deprecation")
    public void setCafeteriaId(Activity mainActivity, int cafeteriaId) {
        mCafeteriaId = cafeteriaId;

        // get all (distinct) dates having menus available
        CafeteriaMenuManager cmm = new CafeteriaMenuManager(mainActivity);
        cursorCafeteriaDates = cmm.getDatesFromDb();
        mainActivity.startManagingCursor(cursorCafeteriaDates);

        for (int position = 0; position < getCount(); position++) {
            cursorCafeteriaDates.moveToPosition(position);
            dates.add(cursorCafeteriaDates.getString(cursorCafeteriaDates.getColumnIndex(Const.ID_COLUMN)));
        }

        // Tell we just update the data
        this.notifyDataSetChanged();
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
        args.putInt(Const.CAFETERIA_ID, mCafeteriaId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Date date = Utils.getDate(dates.get(position));

        DateFormat formatDate = DateFormat.getDateInstance(SimpleDateFormat.FULL);
        return formatDate.format(date)
                         .toUpperCase(Locale.getDefault());
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}