package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.drawer.DrawerMenuHelper;
import de.tum.in.tumcampusapp.component.ui.overview.MainActivity;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

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
    protected View headerView;

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

        setUpLayout();
        setUpDrawer();
        setUpToolbar();
    }

    public void setUpLayout() {
        setContentView(mLayoutId);
    }

    public void setUpDrawer() {
        // Get handles to navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);

        // Setup the navigation drawer if present in the layout
        if (mDrawerList != null && mDrawerLayout != null) {
            // Set personalization in the navdrawer
            headerView = mDrawerList.inflateHeaderView(R.layout.drawer_header);
            TextView nameText = headerView.findViewById(R.id.nameTextView);
            TextView emailText = headerView.findViewById(R.id.emailTextView);

            nameText.setText(Utils.getSetting(this, Const.CHAT_ROOM_DISPLAY_NAME,
                                              getString(R.string.not_connected_to_tumonline)));

            StringBuffer email = new StringBuffer(Utils.getSetting(this, Const.LRZ_ID, ""));
            if (email.toString().isEmpty()) {
                emailText.setVisibility(View.GONE);
            } else {
                email.append("@mytum.de");
            }
            emailText.setText(email);

            // Set picture as set in TUMOnline
            fetchProfilePicture();

            DrawerMenuHelper helper = new DrawerMenuHelper(this, mDrawerLayout);
            helper.populateMenu(mDrawerList.getMenu());

            // Set the NavigationDrawer's click listener
            mDrawerList.setNavigationItemSelectedListener(helper);

            if (Utils.getSettingBool(this, Const.RAINBOW_MODE, false)) {
                headerView.setBackgroundResource(R.drawable.drawer_header_rainbow);
            } else {
                headerView.setBackgroundResource(R.drawable.wear_tuition_fee);
            }
        }
    }

    public void setUpToolbar() {
        String parent = NavUtils.getParentActivityName(this);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null && (parent != null || this instanceof MainActivity)) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStackImmediate();
                return true;
            }
            // Respond to the action bar's Up/Home button
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is NOT part of this apps task, so create a new task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                                .addNextIntentWithParentStack(upIntent)
                                .startActivities();
            } else {
                // This activity is part of this apps task, so simply
                // navigate up to the logical parent activity.
                NavUtils.navigateUpTo(this, upIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchProfilePicture() {
        String id = Utils.getSetting(this, Const.TUMO_PIDENT_NR, "");
        String[] parts = id.split("\\*");
        if (parts.length != 2) {
            return;
        }

        String group = parts[0];
        String personId = parts[1];
        String url = String.format(Locale.getDefault(),
                Const.TUM_ONLINE_PROFILE_PICTURE_URL_FORMAT_STRING, group, personId);

        CircleImageView imageView = headerView.findViewById(R.id.profileImageView);

        Picasso.get()
                .load(url)
                .into(imageView);
    }
}
