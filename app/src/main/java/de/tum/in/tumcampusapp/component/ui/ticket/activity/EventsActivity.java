package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.Arrays;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.component.ui.ticket.fragment.EventsFragment;
import de.tum.in.tumcampusapp.component.ui.ticket.model.EventType;
import de.tum.in.tumcampusapp.utils.Const;

public class EventsActivity extends ActivityForDownloadingExternal {

    private ViewPager viewPager;

    public EventsActivity() {
        super(Const.EVENTS, R.layout.activity_events);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewPager = findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        TabLayout eventTab = findViewById(R.id.event_tab);
        eventTab.setupWithViewPager(viewPager);

        eventTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        EventsViewPagerAdapter adapter = new EventsViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }

    /**
     * This class manages two tabs.
     * One of them shows all available events, the other one all booked events.
     */
    class EventsViewPagerAdapter extends FragmentPagerAdapter {

        EventsViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        private final List<String> titles = Arrays.asList(
                getString(R.string.all_events),
                getString(R.string.booked_events)
        );

        @Override
        public Fragment getItem(int position) {
            EventType type = (position == 0) ? EventType.ALL : EventType.BOOKED;
            return EventsFragment.Companion.newInstance(type);
        }

        @Override
        public int getCount() {
            return titles.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

    }
}


