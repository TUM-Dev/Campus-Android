package de.tum.in.tumcampusapp.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.ListMenuAdapter;
import de.tum.in.tumcampusapp.auxiliary.ListMenuEntry;

/**
 * Activity to show plans
 */
public class PlansActivity extends Activity implements OnItemClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plans);

		ListView list = (ListView) findViewById(R.id.activity_plans_list_view);

		ArrayList<ListMenuEntry> listMenuEntrySet = new ArrayList<ListMenuEntry>();
		listMenuEntrySet.add(new ListMenuEntry(R.drawable.ic_launcher, R.string.campus_garching, R.string.campus_garching_adress, null));
		listMenuEntrySet.add(new ListMenuEntry(R.drawable.ic_launcher, R.string.campus_klinikum, R.string.campus_klinikum_adress, null));
		listMenuEntrySet.add(new ListMenuEntry(R.drawable.ic_launcher, R.string.campus_olympiapark, R.string.campus_olympiapark_adress, null));
		listMenuEntrySet.add(new ListMenuEntry(R.drawable.ic_launcher, R.string.campus_olympiapark_gyms, R.string.campus_olympiapark_adress, null));
		listMenuEntrySet.add(new ListMenuEntry(R.drawable.ic_launcher, R.string.campus_main, R.string.campus_main_adress, null));
		listMenuEntrySet.add(new ListMenuEntry(R.drawable.ic_launcher, R.string.campus_weihenstephan, R.string.campus_weihenstephan_adress, null));
		listMenuEntrySet.add(new ListMenuEntry(R.drawable.ic_launcher, R.string.mvv_fast_train_net, R.string.empty_string, null));
		listMenuEntrySet.add(new ListMenuEntry(R.drawable.ic_launcher, R.string.mvv_nightlines, R.string.empty_string, null));

		ListMenuAdapter adapter = new ListMenuAdapter(this, R.layout.list_layout_complex_small, listMenuEntrySet);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> aview, View view, int pos, long id) {
		Intent intent = new Intent(this, PlansActivityDetails.class);
		intent.putExtra("Plan", pos);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}