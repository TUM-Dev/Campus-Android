package de.tum.in.tumcampusapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.ImplicitCounter;

/**
 * Displays charts generated with GradesActivity
 *
 * NEEDS: chartContent set in incoming bundle (html content to show)
 */
public class GradeChartActivity extends AppCompatActivity {

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
		Webview.loadDataWithBaseURL("file:///android_asset/", chartContent, "text/html", "utf-8", null);
	}
}
