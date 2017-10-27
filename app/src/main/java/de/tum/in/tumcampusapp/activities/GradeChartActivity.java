package de.tum.in.tumcampusapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.auxiliary.ImplicitCounter;

/**
 * Displays charts generated with GradesActivity
 * <p/>
 * NEEDS: chartContent set in incoming bundle (html content to show)
 */
public class GradeChartActivity extends BaseActivity {

    public GradeChartActivity() {
        super(R.layout.activity_gradechart);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.count(this);
        WebView webView = findViewById(R.id.webView1);

        Bundle extras = getIntent().getExtras();
        String chartContent = extras.getString("chartContent");

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webView.requestFocusFromTouch();
        webView.loadDataWithBaseURL("file:///android_asset/", chartContent, "text/html", "utf-8", null);
    }
}
