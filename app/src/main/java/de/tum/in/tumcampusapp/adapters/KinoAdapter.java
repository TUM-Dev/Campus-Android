package de.tum.in.tumcampusapp.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.fragments.KinoDetailsFragment;
import de.tum.in.tumcampusapp.models.tumcabe.Kino;

/**
 * Adapter class for Kino. Handels content for KinoActivity
 */
public class KinoAdapter extends FragmentStatePagerAdapter {

    private final int count; //number of pages
    private final List<String> titles = new ArrayList<>();  // titles shown in the pagerStrip

    public KinoAdapter(FragmentManager fm, Collection<Kino> kinos) {
        super(fm);
        count = kinos.size();

        // get all titles
        for (Kino kino : kinos) {
            titles.add(kino.getTitle());
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
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // returns the titles for the pagerStrip
        String title = titles.get(position);
        return title.substring(title.indexOf(':') + 1)
                    .trim();
    }

}
