package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.models.managers.GalleryManager;

/**
 * Activity to show event details (name, location, image, description, etc.)
 */
public class GalleryActivityDetails extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery_details);

		// get gallery item details from db
		GalleryManager gm = new GalleryManager(this);
		Cursor c = gm.getDetailsFromDb(getIntent().getStringExtra("id"));

		if (!c.moveToNext())
			return;

		String name = c.getString(c.getColumnIndex("name"));
		String image = c.getString(c.getColumnIndex("image"));

		TextView tv = (TextView) findViewById(R.id.activity_gallery_details_infos);
		tv.setText(name);

		WebView browser = (WebView) findViewById(R.id.activity_gallery_details_webView);
		// activate zoom controls
		browser.getSettings().setBuiltInZoomControls(true);
		browser.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
		browser.getSettings().setUseWideViewPort(true);

		String data = "<body style='margin:0px;'><img src='" + image + "'/></body>";
		browser.loadDataWithBaseURL("file:///android_asset/", data, "text/html", "UTF-8", null);
	}
}