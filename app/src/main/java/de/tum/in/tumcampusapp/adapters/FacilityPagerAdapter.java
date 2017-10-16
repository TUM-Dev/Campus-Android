package de.tum.in.tumcampusapp.adapters;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.tum.in.tumcampusapp.fragments.FacilityCategoriesTabFrame;
import de.tum.in.tumcampusapp.fragments.FacilityMyFacilitiesTabFrame;

/**
 * Created by shayansiddiqui on 30.06.17.
 */

public class FacilityPagerAdapter extends FragmentPagerAdapter {

    int tabCount;

    //Constructor to the class
    public FacilityPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        //Initializing tab count
        this.tabCount= tabCount;
    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs
        switch (position) {
            case 0:
                FacilityCategoriesTabFrame tab1 = new FacilityCategoriesTabFrame();
                return tab1;
            case 1:
                FacilityMyFacilitiesTabFrame tab2 = new FacilityMyFacilitiesTabFrame();
                return tab2;
            default:
                return null;
        }
    }

    //Overriden method getCount to get the number of tabs
    @Override
    public int getCount() {
        return tabCount;
    }

}
