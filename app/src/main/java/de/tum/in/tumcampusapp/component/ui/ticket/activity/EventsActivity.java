package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.component.ui.ticket.di.TicketsModule;
import de.tum.in.tumcampusapp.component.ui.ticket.fragment.EventsFragment;
import de.tum.in.tumcampusapp.component.ui.ticket.model.EventType;
import de.tum.in.tumcampusapp.service.DownloadWorker;

public class EventsActivity extends ActivityForDownloadingExternal {

    private String SHOW_BETA_INFO = "ts_show_beta_info";
    private ViewPager viewPager;

    @Inject
    DownloadWorker.Action eventsDownloadAction;

    public EventsActivity() {
        super(R.layout.activity_events);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getInjector().ticketsComponent()
                .ticketsModule(new TicketsModule())
                .build()
                .inject(this);

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

    @Nullable
    @Override
    public DownloadWorker.Action getMethod() {
        return eventsDownloadAction;
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
            return EventsFragment.newInstance(type);
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


