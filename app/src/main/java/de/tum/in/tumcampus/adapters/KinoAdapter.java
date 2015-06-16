package de.tum.in.tumcampus.adapters;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import de.tum.in.tumcampus.fragments.KinoDetailsFragment;

public class KinoAdapter extends FragmentStatePagerAdapter{

    public KinoAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return new KinoDetailsFragment();
    }

    @Override
    public int getCount() {
        return 0;
    }
}
