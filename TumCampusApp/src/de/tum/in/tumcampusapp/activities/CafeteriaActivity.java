package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.managers.CafeteriaManager;

/**
 * Activity to show cafeterias and meals selected by date
 */
public class CafeteriaActivity extends ActivityForDownloadingExternal implements OnItemClickListener {

	/** Current Cafeteria selected */
	private String cafeteriaId;

	/** Current Cafeteria name selected */
	private String cafeteriaName;
	
	private ListView listCafeterias;

	public CafeteriaActivity() {
		super(Const.CAFETERIAS, R.layout.activity_cafeterias);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		listCafeterias = (ListView) findViewById(R.id.listView);
		
		super.requestDownload();
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		Cursor cursorCafeterias = (Cursor) listCafeterias.getAdapter().getItem(position);

		cafeteriaId = cursorCafeterias.getString(cursorCafeterias.getColumnIndex(Const.ID_COLUMN));
		cafeteriaName = cursorCafeterias.getString(cursorCafeterias.getColumnIndex(Const.NAME_COLUMN));

		Intent intent = new Intent(this, CafeteriaDetailsActivity.class);
		intent.putExtra(Const.CAFETERIA_ID, cafeteriaId);
		intent.putExtra(Const.CAFETERIA_NAME, cafeteriaName);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();

		CafeteriaManager cm = new CafeteriaManager(this);
		Cursor cursorCafeterias = cm.getAllFromDb("% %");

		@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapterCafeterias = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, cursorCafeterias, cursorCafeterias.getColumnNames(), new int[] {
				android.R.id.text1, android.R.id.text2 });

		listCafeterias.setAdapter(adapterCafeterias);
		listCafeterias.setOnItemClickListener(this);
	}
}