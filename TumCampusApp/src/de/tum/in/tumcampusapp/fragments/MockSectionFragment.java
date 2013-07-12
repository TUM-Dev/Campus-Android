package de.tum.in.tumcampusapp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.tum.in.tumcampusapp.R;

/**
 * Fragment for each category-page.
 */
public class MockSectionFragment extends Fragment {
	private Activity activity;
	private ListView listViewMenu;

	public MockSectionFragment() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.fragment_mockslidingtab_section, container, false);

		activity = getActivity();
		listViewMenu = (ListView) rootView.findViewById(R.id.listView);

		// Do something with the list
		
		return rootView;
	}
}