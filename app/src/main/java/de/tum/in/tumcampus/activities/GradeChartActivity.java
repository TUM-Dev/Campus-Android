package de.tum.in.tumcampus.activities;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;

/**
 * Displays charts generated with GradesActivity
 * 
 */
public class GradeChartActivity extends ActionBarActivity {

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
		setContentView(R.layout.activity_gradechart);
		WebView Webview = (WebView) findViewById(R.id.webView1);

		Bundle extras = getIntent().getExtras();
		String chartContent = extras.getString("chartContent");

		WebSettings WebSettings = Webview.getSettings();
		WebSettings.setJavaScriptEnabled(true);
		WebSettings.setUseWideViewPort(true);
		WebSettings.setLoadWithOverviewMode(true);
		Webview.requestFocusFromTouch();
		Webview.loadDataWithBaseURL("file:///android_asset/", chartContent,
				"text/html", "utf-8", null);
	}
}
