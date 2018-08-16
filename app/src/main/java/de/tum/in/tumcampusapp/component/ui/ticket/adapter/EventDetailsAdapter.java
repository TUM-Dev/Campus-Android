package de.tum.in.tumcampusapp.component.ui.ticket.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.ticket.fragment.EventDetailsFragment;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;

public class EventDetailsAdapter extends FragmentStatePagerAdapter {

    private List<Event> events;

    public EventDetailsAdapter(FragmentManager fragmentManager, List<Event> events) {
        super(fragmentManager);
        this.events = events;
    }

    @Override
    public Fragment getItem(int position) {
        return EventDetailsFragment.Companion.newInstance(events.get(position).getId());
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
