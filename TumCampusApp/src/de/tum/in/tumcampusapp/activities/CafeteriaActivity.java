package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.managers.CafeteriaManager;

/**
 * Activity to show cafeterias and meals selected by date
 * 
 * @author Sascha Moecker, Thomas Krex
 */

public class CafeteriaActivity extends ActivityForDownloadingExternal implements
		OnItemClickListener {

	/** Current Cafeteria selected */
	private String cafeteriaId;

	/** Current Cafeteria name selected */
	private String cafeteriaName;

	private ListView listCafeterias;
	private SharedPreferences sharedPrefs;
	private Activity activity;

	public CafeteriaActivity() {
		super(Const.CAFETERIAS, R.layout.activity_cafeterias);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Counter();
	
		listCafeterias = (ListView) findViewById(R.id.listView);

		// Fetch cafeteria items on startup
		super.requestDownload(false);
	}
	public void Counter()
	{
		//Counting number of the times that the user used this activity.
		SharedPreferences sp = getSharedPreferences(getString(R.string.MyPrefrences), Activity.MODE_PRIVATE);
		int myvalue = sp.getInt("Cafeteria",0);
		myvalue=myvalue+1;
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt("Cafeteria",myvalue);
		editor.commit();
		
		int myIntValue = sp.getInt("Cafeteria",0);
		Toast.makeText(this, String.valueOf(myIntValue),
				Toast.LENGTH_LONG).show();
				 if(myIntValue==5){
						sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
						SharedPreferences.Editor editor1 = sharedPrefs.edit();
						editor1.putBoolean("menues_id", true);
						editor1.commit();
						editor.putInt("Cafeteria",0);
						editor.commit();
					 
				 
				 }
				 //////////
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		// Get item at clicked position
		Cursor cursorCafeterias = (Cursor) listCafeterias.getAdapter().getItem(
				position);
		startManagingCursor(cursorCafeterias);

		// Get Id and name of the database object
		cafeteriaId = cursorCafeterias.getString(cursorCafeterias
				.getColumnIndex(Const.ID_COLUMN));
		cafeteriaName = cursorCafeterias.getString(cursorCafeterias
				.getColumnIndex(Const.NAME_COLUMN));

		// Put Id and name into an intent and start the detail view of
		// cafeterias
		Intent intent = new Intent(this, CafeteriaDetailsActivity.class);
		intent.putExtra(Const.CAFETERIA_ID, cafeteriaId);
		intent.putExtra(Const.CAFETERIA_NAME, cafeteriaName);

		startActivity(intent);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStart() {
		super.onStart();

		CafeteriaManager cafeteriaManager = new CafeteriaManager(this);

		// Get all available cafeterias from database
		Cursor cursor = cafeteriaManager.getAllFromDb("% %");
		startManagingCursor(cursor);

		// Iterate over all cafeterias and add them to the listview
		if (cursor.getCount() > 0) {
			SimpleCursorAdapter adapterCafeterias = new SimpleCursorAdapter(
					this, R.layout.list_layout_two_line_item, cursor,
					cursor.getColumnNames(), new int[] { android.R.id.text1,
							android.R.id.text2 });

			listCafeterias.setAdapter(adapterCafeterias);
			listCafeterias.setOnItemClickListener(this);
		} else {
			// If something went wrong or no cafeterias found
			super.showErrorLayout();
		}
	}
}
