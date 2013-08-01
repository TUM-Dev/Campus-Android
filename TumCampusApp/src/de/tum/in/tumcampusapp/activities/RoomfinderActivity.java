package de.tum.in.tumcampusapp.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForSearching;
import de.tum.in.tumcampusapp.adapters.RoomFinderListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.FileUtils;
import de.tum.in.tumcampusapp.auxiliary.SearchResultListener;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest;
import de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequestFetchListener;

/**
 * Activity to show a convenience interface for using the MyTUM room finder.
 * 
 * @author Vincenz Doelle
 */
public class RoomfinderActivity extends ActivityForSearching implements
		OnEditorActionListener, SearchResultListener,
		TUMRoomFinderRequestFetchListener, OnItemClickListener {

	// HTTP client for sending requests to MyTUM roomfinder
	//private DefaultHttpClient httpClient;
	TUMRoomFinderRequest roomFinderRequest;

	// the URLs of the MyTUM roomfinder web service
	
	
	// All static variables

    
    
    ListView list;
    RoomFinderListAdapter adapter;

	// private WebView webView;

	public RoomfinderActivity() {
		super(R.layout.activity_roomfinder);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//httpClient = new DefaultHttpClient();
		//webView = Utils.getDefaultWebView(this, R.id.webview_results);
		 roomFinderRequest=new TUMRoomFinderRequest();
	       
		 
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
	}
	
	
	@Override
	public void onSearchResults(String[] result) {
		// Get my results and give them semantics
		String resultCss = result[0];
		String resultExtraction = result[1];

		// Cut the results from the webpage
		resultExtraction = Utils.cutText(resultExtraction,
				"<div id=\"maincontentwrapper\">",
				"<div class=\"documentActions\">");
//		// fit all links
//		resultExtraction = resultExtraction.replace(
//				"<a href=\"search_room_form\">", "<a href=\""
//						+ SERVICE_BASE_URL + "search_room_form\">");
//		resultExtraction = resultExtraction.replace(
//				"<a href=\"search_room_results", "<a href=\""
//						+ SERVICE_BASE_URL + "search_room_results");

		// This buidl the actual html document using the css file and the
		// extracetd results.
		String text = Utils.buildHTMLDocument(resultCss, resultExtraction);

		// write resulting document to temporary file on SD-card
		File file = null;
		try {
			file = FileUtils.getFileOnSD(Const.ROOMFINDER, "tmp.html");
			FileUtils.writeFile(file, text);

			// get image and save it in the same folder as the document
			//FileUtils.getFileFromURL(httpClient, SERVICE_BASE_URL
					//+ "/default.gif",
					//FileUtils.getFileOnSD(Const.ROOMFINDER, "default.gif"));

			//webView.loadUrl("file://" + file.getPath());

			errorLayout.setVisibility(View.GONE);
			progressLayout.setVisibility(View.GONE);
		} catch (Exception e) {
			Toast.makeText(this, R.string.no_sd_card, Toast.LENGTH_SHORT)
					.show();
			Log.e(getClass().getSimpleName(), e.getMessage());

			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean performSearchAlgorithm() {
		
		EditText searchString = (EditText)this.findViewById(R.id.search_field);
		
		
		roomFinderRequest.fetchSearchInteractive(this, this,searchString.getText().toString());
		
//		@SuppressWarnings("deprecation")
//		String param1 = "searchstring="
//				+ URLEncoder.encode(searchField.getText().toString());
//		String param2 = "building=Alle";
//		String param3 = "search=Suche+starten";
//
//		String queryCss = "http://portal.mytum.de/layout.css";
//		String queryExtraction = SERVICE_URL + "?" + param1 + "&" + param2
//				+ "&" + param3;

		//FileUtils.sendAsynchGetRequest(httpClient, this, queryCss,
				//queryExtraction);
		return true;
	}

	@Override
	public void onCommonError(String errorReason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFetch(ArrayList<HashMap<String, String>> result) {
		// TODO Auto-generated method stub
		

        list=(ListView)findViewById(R.id.list);
 
        // Getting adapter by passing xml data ArrayList
        adapter=new RoomFinderListAdapter(this, result);
        list.setAdapter(adapter);
 
        // Click event for single list row
        list.setOnItemClickListener(this);
        
        errorLayout.setVisibility(View.GONE);
		progressLayout.setVisibility(View.GONE);

	}

	@Override
	public void onFetchCancelled() {
		// TODO Auto-generated method stub
		onFetchError("");
	}

	@Override
	public void onFetchError(String errorReason) {
		// TODO Auto-generated method stub
		roomFinderRequest.cancelRequest(true);
		errorLayout.setVisibility(View.VISIBLE);
		progressLayout.setVisibility(View.GONE);

	}

	

	@Override
	public void onSearchError(String message) {
		// TODO Auto-generated method stub
		onFetchError("");
		
	}

	@Override
	public void onFetchMap(Drawable result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
    int position, long id) {
		//buildingId = cursorCafeterias.getString(cursorCafeterias.getColumnIndex(Const.ID_COLUMN));
		//mapId = cursorCafeterias.getString(cursorCafeterias.getColumnIndex(Const.NAME_COLUMN));

		Intent intent = new Intent(this, RoomFinderDetailsActivity.class);
		//intent.putExtra(Const.CAFETERIA_ID, buildingId);
		//intent.putExtra(Const.CAFETERIA_NAME, mapId);

		startActivity(intent);
		
	}
}