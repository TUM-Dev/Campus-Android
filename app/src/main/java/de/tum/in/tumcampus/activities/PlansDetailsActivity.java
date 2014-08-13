package de.tum.in.tumcampus.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebView;
import de.tum.in.tumcampus.R;

/**
 * Activity to show plan details.
 */
public class PlansDetailsActivity extends ActionBarActivity {

	private static int position;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plans_details);

		position = getIntent().getExtras().getInt("Plan");

		WebView browser = (WebView) findViewById(R.id.activity_plans_web_view);
		// activate zoom controls
		browser.getSettings().setBuiltInZoomControls(true);
		// activate double tab to zoom
		browser.getSettings().setUseWideViewPort(true);
		// reset zoom
		browser.clearView();

		// draw image from assets directory in webview
		String file = "";
		int width = getWindowManager().getDefaultDisplay().getWidth();

		if (position == 0) {
			file = "plans/CampusGarching.jpg";
			setTitle(getString(R.string.campus_garching));
			browser.setInitialScale(100 * width / 1024);

		} else if (position == 1) {
			file = "plans/CampusKlinikum.jpg";
			setTitle(getString(R.string.campus_klinikum));
			browser.setInitialScale(100 * width / 1024);

		} else if (position == 2) {
			file = "plans/CampusOlympiapark.jpg";
			setTitle(getString(R.string.campus_olympiapark));
			browser.setInitialScale(100 * width / 900);

		} else if (position == 3) {
			file = "plans/CampusOlympiaparkHallenplan.jpg";
			setTitle(getString(R.string.campus_olympiapark_gyms));
			browser.setInitialScale(100 * width / 800);

		} else if (position == 4) {
			file = "plans/CampusStammgelaende.jpg";
			setTitle(getString(R.string.campus_main));
			browser.setInitialScale(100 * width / 1024);

		} else if (position == 5) {
			file = "plans/CampusWeihenstephan.jpg";
			setTitle(getString(R.string.campus_weihenstephan));
			browser.setInitialScale(100 * width / 1110);

		} else if (position == 6) {
			file = "plans/mvv.png";
			setTitle(getString(R.string.mvv_fast_train_net));
			browser.setInitialScale(100 * width / 1454);

		} else if (position == 7) {
			file = "plans/mvv_night.png";
			setTitle(getString(R.string.mvv_nightlines));
			browser.setInitialScale(100 * width / 1480);

		} else if (position == 8) {
			file = "plans/tram.png";
			setTitle(getString(R.string.mvv_tram));
			browser.setInitialScale(100 * width / 1480);

		} else if (position == 9) {
			file = "plans/mvv_entire_net.png";
			setTitle(getString(R.string.mvv_entire_net));
			browser.setInitialScale(100 * width / 1480);
		}

		String data = "<body style='margin:0px;'><img src='" + file
				+ "'/></body>";
		browser.loadDataWithBaseURL("file:///android_asset/", data,
				"text/html", "UTF-8", null);
		browser.forceLayout();

	}
}