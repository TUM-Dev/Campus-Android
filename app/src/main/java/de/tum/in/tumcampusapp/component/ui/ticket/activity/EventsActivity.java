package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.Arrays;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.component.ui.ticket.fragment.EventsFragment;
import de.tum.in.tumcampusapp.component.ui.ticket.model.EventType;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

public class EventsActivity extends ActivityForDownloadingExternal {

    private String SHOW_BETA_INFO = "ts_show_beta_info";
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

        // Make the beta info only show once, until dismissed. Then hide directly.
        TextView betaInfo = findViewById(R.id.ticket_beta);
        if (Utils.getSettingBool(this, SHOW_BETA_INFO, true)) {
            betaInfo.setOnClickListener(view -> {
                view.setVisibility(View.GONE);
                Utils.setSetting(this, SHOW_BETA_INFO, false);
            });
        } else {
            betaInfo.setVisibility(View.GONE);
        }

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


