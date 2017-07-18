package de.tum.in.tumcampusapp.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.fragments.BarrierfreeFacilityListFragment;

/**
 * The adapter used in ViewPager in BarrierFreeFacilitiesActivity.
 * It passes pageID to BarrierfreeFacilityListFragment to fetch corresponding information.
 */
public class BarrierfreeFacilityAdapter extends FragmentStatePagerAdapter{

    private int currentPageID = 0;

    public BarrierfreeFacilityAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new BarrierfreeFacilityListFragment();
        Bundle args = new Bundle();
        args.putInt(Const.BARRIER_FREE_FACILITY_PAGE_ID, currentPageID);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCurrentPageID(int currentPageID) {
        this.currentPageID = currentPageID;
    }

    @Override
    public int getCount() {
        return 1;
    }
}
