package de.tum.in.tumcampus.activities.generic;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.MainActivity;
import de.tum.in.tumcampus.auxiliary.DrawerMenuHelper;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;

/**
 * Takes care of the navigation drawer which might be attached to the activity and also handles up navigation
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Default layouts for user interaction
     */
    private final int mLayoutId;

    protected DrawerLayout mDrawerLayout;
    protected NavigationView mDrawerList;

    /**
     * Standard constructor for BaseActivity.
     * The given layout might include a DrawerLayout.
     *
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     */
    public BaseActivity(int layoutId) {
        mLayoutId = layoutId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
        setContentView(mLayoutId);

        // Get handles to navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (NavigationView) findViewById(R.id.left_drawer);

        // Setup the navigation drawer if present in the layout
        if (mDrawerList != null) {
            DrawerMenuHelper helper = new DrawerMenuHelper(this, mDrawerLayout);
            helper.populateMenu(mDrawerList.getMenu());

            // Set the NavigationDrawer's click listener
            mDrawerList.setNavigationItemSelectedListener(helper);
        }

        String parent = NavUtils.getParentActivityName(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null && parent != null && parent.equals(MainActivity.class.getName())) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this apps task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                } else {
                    // This activity is part of this apps task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
