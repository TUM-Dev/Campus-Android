package de.tum.in.tumcampus.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.adapters.StartListAdapter;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.ListMenuEntry;

/**
 * Activity to show plans.
 */
public class PlansActivity extends ActionBarActivity implements OnItemClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
		setContentView(R.layout.activity_plans);

		ListView list = (ListView) findViewById(R.id.activity_plans_list_view);

		ArrayList<ListMenuEntry> listMenuEntrySet = new ArrayList<ListMenuEntry>();
		listMenuEntrySet.add(new ListMenuEntry(
				R.drawable.plan_campus_garching_icon, R.string.campus_garching,
				R.string.campus_garching_adress, null));
		listMenuEntrySet.add(new ListMenuEntry(
				R.drawable.plan_campus_klinikum_icon, R.string.campus_klinikum,
				R.string.campus_klinikum_adress, null));
		listMenuEntrySet.add(new ListMenuEntry(
				R.drawable.plan_campus_olympiapark_icon,
				R.string.campus_olympiapark,
				R.string.campus_olympiapark_adress, null));
		listMenuEntrySet.add(new ListMenuEntry(
				R.drawable.plan_campus_olympiapark_hallenplan_icon,
				R.string.campus_olympiapark_gyms,
				R.string.campus_olympiapark_adress, null));
		listMenuEntrySet.add(new ListMenuEntry(
				R.drawable.plan_campus_stammgelaende__icon,
				R.string.campus_main, R.string.campus_main_adress, null));
		listMenuEntrySet.add(new ListMenuEntry(
				R.drawable.plan_campus_weihenstephan_icon,
				R.string.campus_weihenstephan,
				R.string.campus_weihenstephan_adress, null));
		listMenuEntrySet.add(new ListMenuEntry(R.drawable.plan_mvv_icon,
				R.string.mvv_fast_train_net, R.string.empty_string, null));
		listMenuEntrySet.add(new ListMenuEntry(R.drawable.plan_mvv_night_icon,
				R.string.mvv_nightlines, R.string.empty_string, null));
		listMenuEntrySet.add(new ListMenuEntry(R.drawable.plan_tram_icon,
				R.string.mvv_tram, R.string.empty_string, null));
		listMenuEntrySet.add(new ListMenuEntry(R.drawable.mvv_entire_net_icon,
				R.string.mvv_entire_net, R.string.empty_string, null));

		StartListAdapter adapter = new StartListAdapter(this,
				R.layout.list_layout_complex_small, listMenuEntrySet, false);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> aview, View view, int pos, long id) {
		Intent intent = new Intent(this, PlansDetailsActivity.class);
		intent.putExtra("Plan", pos);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}