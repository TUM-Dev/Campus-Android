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

public class EventDetailsAdapter extends FragmentStatePagerAdapter {

    private final List<Event> events = new ArrayList<>();

    public EventDetailsAdapter(FragmentManager fragmentManager, Collection<Event> events) {
        super(fragmentManager);

        this.events.addAll(events);

        this.notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return EventDetailsFragment.newInstance(events.get(position).getId());
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return events.get(position).getTitle().trim();
    }
}
