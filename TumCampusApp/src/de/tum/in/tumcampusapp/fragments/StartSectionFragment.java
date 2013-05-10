package de.tum.in.tumcampusapp.fragments;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.ListMenuAdapter;
import de.tum.in.tumcampusapp.adapters.StartSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.ListMenuEntry;

/**
 * Fragment for each category-page.
 */
public class StartSectionFragment extends Fragment implements OnItemClickListener {

	/**
	 * Contains all list items
	 */
	private ArrayList<ListMenuEntry> listMenuEntrySet;

	public StartSectionFragment() {

	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		listMenuEntrySet = (ArrayList<ListMenuEntry>) getArguments().getSerializable(StartSectionsPagerAdapter.LIST_ENTRY_SET);
		View rootView = inflater.inflate(R.layout.activity_start_fragment_section, container, false);

		// Builds the list according to the list items in listMenuEntrySet
		ListView list = (ListView) rootView.findViewById(R.id.list_view);
		ListMenuAdapter adapter = new ListMenuAdapter(getActivity(), R.layout.list_layout_complex_large, listMenuEntrySet);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		return rootView;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// Starts the corresponding activity via intent
		Intent intent = listMenuEntrySet.get(position).intent;
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}