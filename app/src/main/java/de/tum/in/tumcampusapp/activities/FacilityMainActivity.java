package de.tum.in.tumcampusapp.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForSearching;
import de.tum.in.tumcampusapp.adapters.FacilityPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.FacilityLocatorSuggestionProvider;


/**
 * Activity to fetch and display the curricula of different study programs.
 */
public class FacilityMainActivity extends ActivityForSearching
//        implements OnItemClickListener
{
    public static final String FACILITY_CATEGORY_ID = "category_id";
    public static final String FACILITY_CATEGORY_NAME = "category_name";
    public static final String FACILITY_SEARCH_QUERY="facility_search_query";

    private FloatingActionButton addFacilityButton;

    private TabLayout tabLayout;
    private ViewPager viewPager;


    public FacilityMainActivity() {
        super(R.layout.activity_facility_main, FacilityLocatorSuggestionProvider.AUTHORITY,3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tabLayout=(TabLayout)findViewById(R.id.facility_tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Facility Categories"));
        tabLayout.addTab(tabLayout.newTab().setText("My Facilities"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager=(ViewPager) findViewById(R.id.facility_pager);

        FacilityPagerAdapter facilityPagerAdapter=new FacilityPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(facilityPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab tab=tabLayout.getTabAt(position);
                tab.select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // show the given tab
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // hide the given tab
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // probably ignore this event
            }
        });

        // Sets the adapter

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SearchManager.QUERY)) {
            requestSearch(intent.getStringExtra(SearchManager.QUERY));
            return;
        }

        addFacilityButton = (FloatingActionButton) findViewById(R.id.add_facility);
        addFacilityButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openTaggingActivitty();
            }
        });

    }

    @Override
    protected void onStartSearch() {

    }

    @Override
    protected void onStartSearch(String query) {
        Intent intent = new Intent(this, FacilityActivity.class);
        intent.putExtra(FACILITY_SEARCH_QUERY, query);
        this.startActivity(intent);
    }

    public void openTaggingActivitty() {
        this.startActivity(new Intent(this,FacilityTaggingActivity.class).putExtra(FacilityTaggingActivity.EDIT_MODE,false));
    }
}
