package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import de.tum.in.tumcampusapp.R;

public class GradeChartActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gradechart);
		WebView Webview = (WebView) findViewById(R.id.webView1);
		// WebView pieWebview = (WebView) findViewById(R.id.webView2);
		Bundle extras = getIntent().getExtras();
		String chartContent = extras.getString("chartContent");

		WebSettings WebSettings = Webview.getSettings();
		WebSettings.setJavaScriptEnabled(true);
		WebSettings.setUseWideViewPort(true);
		WebSettings.setLoadWithOverviewMode(true);
		Webview.requestFocusFromTouch();
		Webview.loadDataWithBaseURL("file:///android_asset/", chartContent,
				"text/html", "utf-8", null);

		// String pieContent = extras.getString("pieChart");
		//
		// WebSettings pieWebSettings = pieWebview.getSettings();
		// pieWebSettings.setJavaScriptEnabled(true);
		// pieWebSettings.setUseWideViewPort(true);
		// pieWebSettings.setLoadWithOverviewMode(true);
		// pieWebview.requestFocusFromTouch();
		// pieWebview.loadDataWithBaseURL("file:///android_asset/", pieContent,
		// "text/html", "utf-8", null);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// super.onConfigurationChanged(newConfig);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

}
