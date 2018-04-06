package de.tum.in.tumcampusapp.component.ui.openinghour;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;

/**
 * An activity representing a single Item detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link OpeningHoursListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link OpeningHoursDetailFragment}.
 */
public class OpeningHoursDetailActivity extends BaseActivity {

    public OpeningHoursDetailActivity() {
        super(R.layout.activity_openinghoursdetails);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(OpeningHoursDetailFragment.ARG_ITEM_ID,
                             getIntent().getIntExtra(OpeningHoursDetailFragment.ARG_ITEM_ID, 0));
            arguments.putString(OpeningHoursDetailFragment.ARG_ITEM_CONTENT,
                                getIntent().getStringExtra(OpeningHoursDetailFragment.ARG_ITEM_CONTENT));

            arguments.putBoolean(OpeningHoursDetailFragment.TWO_PANE,
                                 getIntent().getBooleanExtra(OpeningHoursDetailFragment.TWO_PANE, false));
            OpeningHoursDetailFragment fragment = new OpeningHoursDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                                       .add(R.id.item_detail_container, fragment)
                                       .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, OpeningHoursListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
