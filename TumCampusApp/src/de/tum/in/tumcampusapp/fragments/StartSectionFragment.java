package de.tum.in.tumcampusapp.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampusapp.activities.LecturesSearchActivity;
import de.tum.in.tumcampusapp.activities.PersonsSearchActivity;
import de.tum.in.tumcampusapp.adapters.StartListAdapter;
import de.tum.in.tumcampusapp.adapters.StartSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.ListMenuEntry;

/**
 * Fragment for each start-category-page.
 */
public class StartSectionFragment extends Fragment implements
		OnItemClickListener {

	private int imageId;
	private Activity activity;
	private SharedPreferences sharedPrefs;
	ImageView instruction_overlay_cross;
	Button overlay_button;
	ImageView myTUM_overlay_cross;
	ImageView news_overlay_cross;

	OnClickListener abortTutorial;

	private View rootView;

	private RelativeLayout instruction_overlay;
	private RelativeLayout myTUM_overlay;
	private RelativeLayout news_overlay;

	/**
	 * Contains all list items
	 */
	private ArrayList<ListMenuEntry> listMenuEntrySet;

	public StartSectionFragment() {

	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = getActivity();
		listMenuEntrySet = (ArrayList<ListMenuEntry>) getArguments()
				.getSerializable(StartSectionsPagerAdapter.LIST_ENTRY_SET);
		imageId = getArguments().getInt(
				StartSectionsPagerAdapter.IMAGE_FOR_CATEGORY);
		if (Build.VERSION.SDK_INT >= 11)
			rootView = inflater.inflate(
					R.layout.fragment_start_section_overlay_actionbar,
					container, false);
		else
			rootView = inflater.inflate(
					R.layout.fragment_start_section_overlay_menubutton,
					container, false);

		// views for the overlays
		instruction_overlay = (RelativeLayout) rootView
				.findViewById(R.id.instruction_overlay);
		overlay_button = (Button) rootView.findViewById(R.id.continue_btn);

		instruction_overlay_cross = (ImageView) rootView
				.findViewById(R.id.instruction_cross);

		myTUM_overlay_cross = (ImageView) rootView
				.findViewById(R.id.myTum_cross);

		news_overlay_cross = (ImageView) rootView.findViewById(R.id.news_cross);


		myTUM_overlay = (RelativeLayout) rootView
				.findViewById(R.id.myTUM_overlay);
		news_overlay = (RelativeLayout) rootView
				.findViewById(R.id.news_overlay);
	
		abortTutorial = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sharedPrefs.edit().putBoolean(Const.FIRST_RUN, false).commit();
				myTUM_overlay.setVisibility(View.GONE);
				news_overlay.setVisibility(View.GONE);
				instruction_overlay.setVisibility(View.GONE);
				

			}
		};

		// abort tutorial by clicking the cross
		instruction_overlay_cross.setOnClickListener(abortTutorial);

		myTUM_overlay_cross.setOnClickListener(abortTutorial);

		news_overlay_cross.setOnClickListener(abortTutorial);
		
		

		// Builds the list according to the list items in listMenuEntrySet
		GridView list = (GridView) rootView.findViewById(R.id.gridview);
		//ListView list = (ListView) rootView.findViewById(R.id.list_view);
		ImageView image = (ImageView) rootView.findViewById(R.id.img_category);
		image.setImageResource(imageId);
		

		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
	
		StartListAdapter adapter = new StartListAdapter(getActivity(),
				R.layout.list_layout_complex_large, listMenuEntrySet, true);
		
		
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

		return rootView;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// Starts the corresponding activity via intent
		Intent intent = listMenuEntrySet.get(position).intent;
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		GridView list = (GridView) rootView.findViewById(R.id.gridview);
		ImageView image = (ImageView) rootView.findViewById(R.id.img_category);
		image.setImageResource(imageId);
		StartListAdapter adapter = new StartListAdapter(getActivity(),
				R.layout.list_layout_complex_large, listMenuEntrySet, true);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);

		switch (getArguments().getInt("POSITION")) {
		
		case StartSectionsPagerAdapter.SECTION_PERSONALIZED:
			if (sharedPrefs.getBoolean(Const.FIRST_RUN, true)) {

				instruction_overlay.setVisibility(View.VISIBLE);


				overlay_button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						instruction_overlay.setVisibility(View.GONE);
						myTUM_overlay.setVisibility(View.VISIBLE);

					}
				});
			} else {
				instruction_overlay.setVisibility(View.GONE);
				myTUM_overlay.setVisibility(View.GONE);
				news_overlay.setVisibility(View.GONE);


			}

			break;
		case StartSectionsPagerAdapter.SECTION_NEWS:
			if (sharedPrefs.getBoolean(Const.FIRST_RUN, true))
				news_overlay.setVisibility(View.VISIBLE);
			else
				news_overlay.setVisibility(View.GONE);

		}
		rootView.invalidate();

	}
}
