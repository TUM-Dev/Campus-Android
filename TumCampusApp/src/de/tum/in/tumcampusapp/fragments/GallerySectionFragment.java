package de.tum.in.tumcampusapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.GalleryDetailsActivity;
import de.tum.in.tumcampusapp.adapters.GallerySectionsPagerAdapter;
import de.tum.in.tumcampusapp.models.managers.GalleryManager;

/**
 * Fragment for each gallery-category-page.
 */
public class GallerySectionFragment extends Fragment implements
		OnItemClickListener {

	private Activity activity;
	private RelativeLayout errorLayout;

	public GallerySectionFragment() {
	}

	@SuppressWarnings({ "deprecation" })
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_gallery_section,
				container, false);

		activity = getActivity();
		errorLayout = (RelativeLayout) rootView.findViewById(R.id.error_layout);

		int galleryMode = getArguments().getInt(
				GallerySectionsPagerAdapter.ARG_GALLERY_MODE);

		// Gets images from database
		GalleryManager gm = new GalleryManager(activity);
		SimpleCursorAdapter adapter;
		Cursor cursor;

		switch (galleryMode) {
		case GallerySectionsPagerAdapter.PAGE_LATESTS_GALLERY:
			cursor = gm.getFromDb();
			break;
		case GallerySectionsPagerAdapter.PAGE_PAST_GALLERY:
			cursor = gm.getFromDbArchive();
			break;
		default:
			cursor = gm.getFromDb();
			break;

		}

		activity.startManagingCursor(cursor);

		if (cursor.getCount() > 0) {
			adapter = new SimpleCursorAdapter(activity,
					R.layout.activity_gallery_image, cursor,
					cursor.getColumnNames(),
					new int[] { R.id.activity_gallery_image });

			GridView gridview = (GridView) rootView
					.findViewById(R.id.activity_gallery_gridview);
			gridview.setAdapter(adapter);
			gridview.setOnItemClickListener(this);

			// Resets new items counter
			GalleryManager.lastInserted = 0;
		} else {
			errorLayout.setVisibility(View.VISIBLE);
		}

		return rootView;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		Cursor cursor = (Cursor) av.getAdapter().getItem(position);
		activity.startManagingCursor(cursor);

		// Opens gallery details when clicking an item in the list
		Intent intent = new Intent(activity, GalleryDetailsActivity.class);
		intent.putExtra("id", cursor.getString(cursor.getColumnIndex("id")));
		startActivity(intent);
	}
}