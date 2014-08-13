package de.tum.in.tumcampus.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.tum.in.tumcampus.R;

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

		getArguments().getInt("key");

		// Do something with the list

		return rootView;
	}
}