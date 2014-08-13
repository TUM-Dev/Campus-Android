package de.tum.in.tumcampus.fragments;

import java.util.HashMap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.CafetariaPrices;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.managers.LocationManager;

/**
 * Fragment for each cafeteria-page.
 */
public class CafeteriaDetailsSectionFragment extends Fragment {
	private Activity activity;
	private String cafeteriaId;
	private String cafeteriaName;
	private String date;
	private RelativeLayout errorLayout;
	private View rootView;
	private View footer;
	private ListView listViewMenu;
	private SharedPreferences sharedPrefs;

	public CafeteriaDetailsSectionFragment() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.fragment_cafeteriadetails_section, container, false);

		activity = getActivity();

		listViewMenu = (ListView) rootView.findViewById(R.id.listView);
		errorLayout = (RelativeLayout) rootView.findViewById(R.id.error_layout);

		date = getArguments().getString(Const.DATE);
		cafeteriaId = getArguments().getString(Const.CAFETERIA_ID);
		cafeteriaName = getArguments().getString(Const.CAFETERIA_NAME);

		// initialize listview footer for opening hours
		footer = getLayoutInflater(savedInstanceState).inflate(
				android.R.layout.two_line_list_item, null, false);

		showMenueForDay();
		this.rootView = rootView;
		return rootView;
	}

	@SuppressWarnings("deprecation")
	private void showMenueForDay() {
		TextView textView;

		// opening hours
		LocationManager lm = new LocationManager(activity);
		textView = (TextView) footer.findViewById(android.R.id.text2);
		textView.setText(lm.getHoursById(cafeteriaId));

		// menus
		CafeteriaMenuManager cmm = new CafeteriaMenuManager(getActivity());
		Cursor cursorCafeteriaMenu = cmm.getTypeNameFromDb(cafeteriaId, date);
		activity.startManagingCursor(cursorCafeteriaMenu);

		textView = (TextView) footer.findViewById(android.R.id.text1);
		if (cursorCafeteriaMenu.getCount() == 0) {
			textView.setText(getString(R.string.opening_hours));
			errorLayout.setVisibility(View.VISIBLE);
		} else {
			textView.setText(getString(R.string.kitchen_opening));
			errorLayout.setVisibility(View.GONE);
		}

		// no onclick for items, no separator line
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapterMenue = new SimpleCursorAdapter(activity,
				R.layout.list_layout_cafeteriamenu, cursorCafeteriaMenu,
				cursorCafeteriaMenu.getColumnNames(), new int[] {
						R.id.tx_category, R.id.tx_menu, R.id.tx_price }) {

		};
		adapterMenue.setViewBinder(new ViewBinder() {

			// Adding prices not to the database, but manually to the list
			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {

				if (view.getId() == R.id.tx_price) {
					TextView price = (TextView) view;

					String curKey = cursor.getString(cursor
							.getColumnIndex("typeLong"));

					sharedPrefs = PreferenceManager
							.getDefaultSharedPreferences(activity);

					HashMap<String, String> rolePrices = null;
					String type = sharedPrefs.getString(Const.ROLE, "0");
					if (type.equals("0")) {
						rolePrices = CafetariaPrices.student_prices;
					} else if (type.equals("1")) {
						rolePrices = CafetariaPrices.employee_prices;
					} else if (type.equals("2")) {
						rolePrices = CafetariaPrices.guest_prices;
					} else {
						rolePrices = CafetariaPrices.student_prices;
					}

					if (rolePrices.containsKey(curKey))
						price.setText(rolePrices.get(curKey) + " €");

					else
						price.setText("");

					// set price field invisible for "Beilagen"
					if (curKey.equals("Beilagen"))
						price.setVisibility(View.GONE);

					return true;
				}
				return false;
			}
		});
		listViewMenu.setAdapter(adapterMenue);
	}
}
