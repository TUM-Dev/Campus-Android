package de.tum.in.tumcampusapp.fragments;

import java.util.regex.Pattern;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.OpeningHoursDetailActivity;
import de.tum.in.tumcampusapp.activities.OpeningHoursListActivity;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.data.LocationContent;
import de.tum.in.tumcampusapp.models.managers.LocationManager;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link OpeningHoursListActivity} in two-pane mode (on tablets)
 * or a {@link OpeningHoursDetailActivity} on handsets.
 */
public class OpeningHoursDetailFragment extends Fragment implements ViewBinder {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	public static final String TWO_PANE = "two_pane";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private LocationContent.Location mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public OpeningHoursDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = LocationContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
		}
		if (getArguments().containsKey(TWO_PANE)) {
			if (!getArguments().getBoolean(TWO_PANE)) {
				getActivity().setTitle(getActivity().getTitle() + " for " + mItem.content);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

		// click on category in list
		LocationManager lm = new LocationManager(getActivity());
		String[] categories = getString(R.string.facility_categories_splitted).split(",");
		Cursor c = lm.getAllHoursFromDb(categories[Integer.valueOf(mItem.id)]);

		@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.two_line_list_item, c, c.getColumnNames(), new int[] {
				android.R.id.text1, android.R.id.text2 }) {

			@Override
			public boolean isEnabled(int position) {
				// disable onclick
				return false;
			}
		};
		adapter.setViewBinder(this);

		ListView lv2 = (ListView) rootView.findViewById(R.id.fragment_item_detail_listview);
		lv2.setAdapter(adapter);

		return rootView;
	}

	/**
	 * change presentation of locations in the list
	 */
	@Override
	public boolean setViewValue(View view, Cursor c, int index) {
		if (view.getId() == android.R.id.text2) {
			String transport = c.getString(c.getColumnIndex(Const.TRANSPORT_COLUMN));
			String address = c.getString(c.getColumnIndex(Const.ADDRESS_COLUMN));

			String hours = c.getString(c.getColumnIndex(Const.HOURS_COLUMN));
			String remark = c.getString(c.getColumnIndex(Const.REMARK_COLUMN));
			String room = c.getString(c.getColumnIndex(Const.ROOM_COLUMN));

			StringBuilder sb = new StringBuilder(hours + "\n" + address);
			if (room.length() > 0) {
				sb.append(", " + room);
			}
			if (transport.length() > 0) {
				sb.append(" (" + transport + ")");
			}
			if (remark.length() > 0) {
				sb.append("\n" + remark.replaceAll("\\\\n", "\n"));
			}
			TextView tv = (TextView) view;
			tv.setText(sb.toString());

			// linkify email addresses and phone numbers (e.g. 089-123456)
			// don't linkify room numbers 00.01.123
			Linkify.addLinks(tv, Linkify.EMAIL_ADDRESSES);
			Linkify.addLinks(tv, Pattern.compile("[0-9-]{6,}"), "tel:");
			return true;
		}
		return false;
	}
}
