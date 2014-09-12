package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.adapters.StartListAdapter;
import de.tum.in.tumcampus.adapters.StartListAdapter.PlanListEntry;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;

/**
 * Activity to show plans.
 */
public class PlansActivity extends ActionBarActivity implements OnItemClickListener {

    private StartListAdapter mListAdapter;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
		setContentView(R.layout.activity_plans);

		ListView list = (ListView) findViewById(R.id.activity_plans_list_view);
		ArrayList<PlanListEntry> listMenuEntrySet = new ArrayList<PlanListEntry>();
		listMenuEntrySet.add(new PlanListEntry(
				R.drawable.plan_campus_garching_icon, R.string.campus_garching,
				R.string.campus_garching_adress, R.drawable.campus_garching));
		listMenuEntrySet.add(new PlanListEntry(
				R.drawable.plan_campus_klinikum_icon, R.string.campus_klinikum,
				R.string.campus_klinikum_adress, R.drawable.campus_klinikum));
		listMenuEntrySet.add(new StartListAdapter.PlanListEntry(
				R.drawable.plan_campus_olympiapark_icon,
				R.string.campus_olympiapark,
				R.string.campus_olympiapark_adress, R.drawable.campus_olympiapark));
		listMenuEntrySet.add(new PlanListEntry(
				R.drawable.plan_campus_olympiapark_hallenplan_icon,
				R.string.campus_olympiapark_gyms,
				R.string.campus_olympiapark_adress, R.drawable.campus_olympiapark_hallenplan));
		listMenuEntrySet.add(new PlanListEntry(
				R.drawable.plan_campus_stammgelaende__icon,
				R.string.campus_main, R.string.campus_main_adress, R.drawable.campus_stammgelaende));
		listMenuEntrySet.add(new PlanListEntry(
				R.drawable.plan_campus_weihenstephan_icon,
				R.string.campus_weihenstephan,
				R.string.campus_weihenstephan_adress, R.drawable.campus_weihenstephan));
		listMenuEntrySet.add(new PlanListEntry(R.drawable.plan_mvv_icon,
				R.string.mvv_fast_train_net, R.string.empty_string, R.drawable.mvv));
		listMenuEntrySet.add(new PlanListEntry(R.drawable.plan_mvv_night_icon,
				R.string.mvv_nightlines, R.string.empty_string, R.drawable.mvv_night));
		listMenuEntrySet.add(new PlanListEntry(R.drawable.plan_tram_icon,
				R.string.mvv_tram, R.string.empty_string, R.drawable.tram));
		listMenuEntrySet.add(new PlanListEntry(R.drawable.mvv_entire_net_icon,
				R.string.mvv_entire_net, R.string.empty_string, R.drawable.mvv_entire_net));

		mListAdapter = new StartListAdapter(this, R.layout.activity_plans_listview, listMenuEntrySet);
		list.setAdapter(mListAdapter);
		list.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> aview, View view, int pos, long id) {
		Intent intent = new Intent(this, PlansDetailsActivity.class);
        PlanListEntry entry = (PlanListEntry) mListAdapter.getItem(pos);
        intent.putExtra(PlansDetailsActivity.PLAN_TITLE_ID, entry.titleId);
		intent.putExtra(PlansDetailsActivity.PLAN_IMG_ID, entry.imgId);
		startActivity(intent);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}