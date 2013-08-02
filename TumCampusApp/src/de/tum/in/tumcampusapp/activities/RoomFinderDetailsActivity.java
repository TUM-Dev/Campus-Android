package de.tum.in.tumcampusapp.activities;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.CafeteriaDetailsSectionsPagerAdapter;
import de.tum.in.tumcampusapp.adapters.RoomFinderDetailsSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;

public class RoomFinderDetailsActivity extends FragmentActivity {

	private String cafeteriaId;
	private String cafeteriaName;
	private WebView browser;
	private RelativeLayout errorLayout;
	/** Http client to fetch the curricula data */
	private DefaultHttpClient httpClient;
	private RelativeLayout progressLayout;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_roomfinderdetails);

		httpClient = new DefaultHttpClient();

		browser = (WebView) findViewById(R.id.activity_roomfinder_web_view);
		progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
		errorLayout = (RelativeLayout) findViewById(R.id.error_layout);

		browser.getSettings().setBuiltInZoomControls(true);
		browser.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
		browser.getSettings().setUseWideViewPort(true);
		
		String buildingId = getIntent().getExtras().getString("buildingId");
		String roomId = getIntent().getExtras().getString("roomId");
		String mapId = getIntent().getExtras().getString("mapId");
		
		
		
		//browser.loadUrl("http://vmbaumgarten3.informatik.tu-muenchen.de/roommaps/building/defaultMap?id="+buildingId);
		browser.loadUrl("http://vmbaumgarten3.informatik.tu-muenchen.de/roommaps/room/map?id="+roomId+"&mapid="+mapId);

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(
				R.menu.menu_section_fragment_cafeteria_details, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//PersonalLayoutManager.setColorForId(this, R.id.pager_title_strip);
	}

}
