package de.tum.in.tumcampusapp.component.ui.ticket.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tum.in.tumcampusapp.component.ui.ticket.fragment.EventDetailsFragment;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.utils.Const;

public class EventDetailsAdapter extends FragmentStatePagerAdapter {

    private final int count; //number of pages
    private final List<String> titles = new ArrayList<>();  // titles shown in the pagerStrip

    public EventDetailsAdapter(FragmentManager fm, Collection<Event> events) {
        super(fm);
        count = events.size();

        // get all titles
        for (Event event : events) {
            titles.add(event.getTitle());
        }

        this.notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new EventDetailsFragment();
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
    public CharSequence getPageTitle(int position) {
        // returns the titles for the pagerStrip
        return titles.get(position).trim();
    }
}
