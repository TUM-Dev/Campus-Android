package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import de.tum.in.tumcampusapp.R;

/**
 * Activity to show plans
 */
public class PlansActivityDetails extends Activity {

	private static int position;

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
		@SuppressWarnings("deprecation")
		int width = getWindowManager().getDefaultDisplay().getWidth();

		if (position == 0) {
			file = "plans/CampusGarching.jpg";
			setTitle(getString(R.string.plan_for) + " " + getString(R.string.campus_garching));
			browser.setInitialScale(100 * width / 1024);

		} else if (position == 1) {
			file = "plans/CampusKlinikum.jpg";
			setTitle(getString(R.string.plan_for) + " " + getString(R.string.campus_klinikum));
			browser.setInitialScale(100 * width / 1024);

		} else if (position == 2) {
			file = "plans/CampusOlympiapark.jpg";
			setTitle(getString(R.string.plan_for) + " " + getString(R.string.campus_olympiapark));
			browser.setInitialScale(100 * width / 900);

		} else if (position == 3) {
			file = "plans/CampusOlympiaparkHallenplan.jpg";
			setTitle(getString(R.string.plan_for) + " " + getString(R.string.campus_olympiapark_gyms));
			browser.setInitialScale(100 * width / 800);

		} else if (position == 4) {
			file = "plans/CampusStammgelaende.jpg";
			setTitle(getString(R.string.plan_for) + " " + getString(R.string.campus_main));
			browser.setInitialScale(100 * width / 1024);

		} else if (position == 5) {
			file = "plans/CampusWeihenstephan.jpg";
			setTitle(getString(R.string.plan_for) + " " + getString(R.string.campus_weihenstephan));
			browser.setInitialScale(100 * width / 1110);

		} else if (position == 6) {
			file = "plans/mvv.jpg";
			setTitle(getString(R.string.plan_for) + " " + getString(R.string.mvv_fast_train_net));
			browser.setInitialScale(100 * width / 1454);

		} else {
			file = "plans/mvv_night.jpg";
			setTitle(getString(R.string.plan_for) + " " + getString(R.string.mvv_nightlines));
			browser.setInitialScale(100 * width / 1480);
		}

		String data = "<body style='margin:0px;'><img src='" + file + "'/></body>";
		browser.loadDataWithBaseURL("file:///android_asset/", data, "text/html", "UTF-8", null);
		browser.forceLayout();

	}
}