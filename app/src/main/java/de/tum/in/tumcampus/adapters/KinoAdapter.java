package de.tum.in.tumcampus.adapters;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.fragments.KinoDetailsFragment;

/**
 * Adapter class for Kino. Handels content for KinoActivity
 */
public class KinoAdapter extends FragmentStatePagerAdapter{

    private int count; //number of pages
    private ArrayList<String> titles = new ArrayList<>();  // titles shown in the pagerStrip

    public KinoAdapter(FragmentManager fm, Cursor cursor){
        super(fm);
        count = cursor.getCount();

        // get all titles
        for (int i = 0; i < getCount(); i++){
            cursor.moveToPosition(i);
            titles.add(i, cursor.getString(cursor.getColumnIndex(Const.JSON_TITLE)));
        }

        this.notifyDataSetChanged();
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

    @Override
    public int getItemPosition(Object object){
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position){
        // returns the titles for the pagerStrip
        String title = titles.get(position);
        return title.substring(title.indexOf(':') + 1).trim();
    }

}
