package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.managers.GalleryManager;

/**
 * Activity to show gallery items (name, image, etc.)
 */
public class GalleryActivity extends ActivityForDownloadingExternal implements OnItemClickListener {

	public GalleryActivity() {
		super(Const.GALLERY, R.layout.activity_gallery);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.requestDownload();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		Cursor cursor = (Cursor) av.getAdapter().getItem(position);
		startManagingCursor(cursor);

		// Opens gallery details when clicking an item in the list
		Intent intent = new Intent(this, GalleryDetailsActivity.class);
		intent.putExtra("id", cursor.getString(cursor.getColumnIndex("id")));
		startActivity(intent);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStart() {
		super.onStart();

		// Gets images from database
		GalleryManager gm = new GalleryManager(this);
		Cursor cursor = gm.getFromDb();
		startManagingCursor(cursor);
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.activity_gallery_image, cursor, cursor.getColumnNames(),
				new int[] { R.id.activity_gallery_image });

		GridView gridview = (GridView) findViewById(R.id.activity_gallery_gridview);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(this);

		// Resets new items counter
		GalleryManager.lastInserted = 0;
	}
}