package de.tum.in.tumcampus.adapters;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.fragments.KinoDetailsFragment;

/**
 * Adapter class for Kino. Handels content for KinoActivity
 */
public class KinoAdapter extends FragmentStatePagerAdapter{

    private int count; //number of pages

    public KinoAdapter(FragmentManager fm, int count){
        super(fm);
        this.count = count;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new KinoDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(Const.POSITION, position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return count;
    }
}
