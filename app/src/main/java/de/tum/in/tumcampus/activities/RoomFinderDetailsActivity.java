package de.tum.in.tumcampus.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;

/**
 * Displays the map regarding the searched room.
 * 
 */
public class RoomFinderDetailsActivity extends ActionBarActivity {

    @SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
		setContentView(R.layout.activity_roomfinderdetails);

        WebView browser = (WebView) findViewById(R.id.activity_roomfinder_web_view);
        browser.getSettings().setBuiltInZoomControls(true);
		browser.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
		browser.getSettings().setUseWideViewPort(true);

		String roomId = getIntent().getExtras().getString("roomId");
		String mapId = getIntent().getExtras().getString("mapId");

		browser.loadUrl("http://vmbaumgarten3.informatik.tu-muenchen.de/roommaps/room/map?id="
                + roomId + "&mapid=" + mapId);
	}
}
