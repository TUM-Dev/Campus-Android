package de.tum.in.tumcampusapp.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.managers.CafeteriaMenuManager;
import de.tum.in.tumcampusapp.models.managers.LocationManager;

/**
 * Fragment for each category-page.
 */
public class CafeteriaDetailsSectionFragment extends Fragment {
	private Activity activity;
	private String cafeteriaId;
	private String cafeteriaName;
	private String date;
	private View footer;
	private ListView listViewMenu;

	public CafeteriaDetailsSectionFragment() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_cafeteriadetails_section, container, false);

		activity = getActivity();
		listViewMenu = (ListView) rootView.findViewById(R.id.listView);

		date = getArguments().getString(Const.DATE);
		cafeteriaId = getArguments().getString(Const.CAFETERIA_ID);
		cafeteriaName = getArguments().getString(Const.CAFETERIA_NAME);

		// initialize listview footer for opening hours
		footer = getLayoutInflater(savedInstanceState).inflate(android.R.layout.two_line_list_item, null, false);

		listViewMenu.addFooterView(footer, null, false);
		showMenueForDay();
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
		} else {
			textView.setText(getString(R.string.kitchen_opening));
		}

		// no onclick for items, no separator line
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapterMenue = new SimpleCursorAdapter(activity, android.R.layout.two_line_list_item, cursorCafeteriaMenu,
				cursorCafeteriaMenu.getColumnNames(), new int[] { android.R.id.text1, android.R.id.text2 }) {

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