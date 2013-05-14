package de.tum.in.tumcampusapp.activities;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampusapp.models.managers.LocationManager;

public class CafeteriaDetailsActivity extends Activity implements OnItemClickListener {
	/** Current Date selected (ISO format) */
	private static String date;
	/** Current Date selected (German format) */
	@SuppressWarnings("unused")
	private static String dateStr;
	/** Footer with opening hours */
	View footer;
	private String cafeteriaId;
	private String cafeteriaName;
	
	private ListView listViewMenu;
	private ListView listViewDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cafeteriasdetails);

		// default date: today or next monday if today is weekend
		if (date == null) {
			Calendar calendar = Calendar.getInstance();
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek == Calendar.SATURDAY) {
				calendar.add(Calendar.DATE, 2);
			}
			if (dayOfWeek == Calendar.SUNDAY) {
				calendar.add(Calendar.DATE, 1);
			}
			date = Utils.getDateString(calendar.getTime());
			dateStr = Utils.getDateStringDe(calendar.getTime());
		}

		cafeteriaId = getIntent().getExtras().getString(Const.CAFETERIA_ID);
		cafeteriaName = getIntent().getExtras().getString(Const.CAFETERIA_NAME);

		// initialize listview footer for opening hours
		footer = getLayoutInflater().inflate(android.R.layout.two_line_list_item, null, false);

		listViewMenu = (ListView) findViewById(R.id.listView);
		listViewDate = (ListView) findViewById(R.id.listView_DELETE);
		listViewMenu.addFooterView(footer);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// get all (distinct) dates having menus available
		CafeteriaMenuManager cmm = new CafeteriaMenuManager(this);
		Cursor cursorDates = cmm.getDatesFromDb();

		@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapterCafeteriasTimes = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorDates, cursorDates.getColumnNames(),
				new int[] { android.R.id.text1 });

		listViewDate.setAdapter(adapterCafeteriasTimes);
		listViewDate.setOnItemClickListener(this);

		setTitle(cafeteriaName);
		showMenueForDay();

		// reset new items counter
		CafeteriaMenuManager.lastInserted = 0;
	}

	@SuppressLint("CutPasteId")
	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		// click on date
		if (av.getId() == R.id.listView_DELETE) {
			Cursor cursorDateItem = (Cursor) listViewDate.getAdapter().getItem(position);
			date = cursorDateItem.getString(cursorDateItem.getColumnIndex(Const.ID_COLUMN));
			dateStr = cursorDateItem.getString(cursorDateItem.getColumnIndex(Const.DATE_COLUMN_DE));
		}
		showMenueForDay();
	}

	private void showMenueForDay() {
		TextView textView;

		// get menus filtered by cafeteria and date
		if (cafeteriaId != null && date != null) {
			// opening hours
			LocationManager lm = new LocationManager(this);
			textView = (TextView) footer.findViewById(android.R.id.text2);
			textView.setText(lm.getHoursById(cafeteriaId));

			// menus
			CafeteriaMenuManager cmm = new CafeteriaMenuManager(this);
			Cursor cursorMenu = cmm.getTypeNameFromDb(cafeteriaId, date);

			textView = (TextView) footer.findViewById(android.R.id.text1);
			if (cursorMenu.getCount() == 0) {
				textView.setText(getString(R.string.opening_hours));
			} else {
				textView.setText(getString(R.string.kitchen_opening));
			}

			// no onclick for items, no separator line
			@SuppressWarnings("deprecation")
			SimpleCursorAdapter adapterMenue = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, cursorMenu, cursorMenu.getColumnNames(), new int[] {
					android.R.id.text1, android.R.id.text2 }) {

				@Override
				public boolean areAllItemsEnabled() {
					return false;
				}

				@Override
				public boolean isEnabled(int position) {
					return false;
				}
			};
			listViewMenu.setAdapter(adapterMenue);
		}
	}
}
