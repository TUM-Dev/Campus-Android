package de.tum.in.tumcampusapp.activities;

import java.io.File;
import java.net.URLEncoder;

import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForSearching;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.FileUtils;
import de.tum.in.tumcampusapp.auxiliary.SearchResultListener;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Activity to show a convenience interface for using the MyTUM room finder.
 * 
 * @author Vincenz Doelle
 */
public class RoomfinderActivity extends ActivityForSearching implements OnEditorActionListener, SearchResultListener {

	// HTTP client for sending requests to MyTUM roomfinder
	private DefaultHttpClient httpClient;

	// the URLs of the MyTUM roomfinder web service
	private final String SERVICE_BASE_URL = "http://portal.mytum.de/campus/roomfinder/";
	private final String SERVICE_URL = SERVICE_BASE_URL + "search_room_results";

	private WebView webView;

	public RoomfinderActivity() {
		super(R.layout.activity_roomfinder);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		httpClient = new DefaultHttpClient();
		webView = Utils.getDefaultWebView(this, R.id.webview_results);
	}

	@Override
	public void onSearchResults(String[] results) {
		String text = "";

		try {
			// Get my results and give them semantics
			String resultCss = results[0];
			String resultExtraction = results[1];

			// Cut the results from the webpage
			resultExtraction = Utils.cutText(resultExtraction, "<div id=\"maincontentwrapper\">", "<div class=\"documentActions\">");
			// fit all links
			resultExtraction = resultExtraction.replace("<a href=\"search_room_form\">", "<a href=\"" + SERVICE_BASE_URL + "search_room_form\">");
			resultExtraction = resultExtraction.replace("<a href=\"search_room_results", "<a href=\"" + SERVICE_BASE_URL + "search_room_results");

			// This buidl the actual html document using the css file and the
			// extracetd results.
			text = Utils.buildHTMLDocument(resultCss, resultExtraction);

		} catch (Exception e) {
			Toast.makeText(this, R.string.exception_unknown, Toast.LENGTH_SHORT).show();
			Log.e(getClass().getSimpleName(), e.getMessage());

			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
		}

		// write resulting document to temporary file on SD-card
		File file = null;
		try {
			file = FileUtils.getFileOnSD(Const.ROOMFINDER, "tmp.html");
			FileUtils.writeFile(file, text);

			// get image and save it in the same folder as the document
			FileUtils.getFileFromURL(httpClient, SERVICE_BASE_URL + "/default.gif", FileUtils.getFileOnSD(Const.ROOMFINDER, "default.gif"));

			webView.loadUrl("file://" + file.getPath());

			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
		} catch (Exception e) {
			Toast.makeText(this, R.string.no_sd_card, Toast.LENGTH_SHORT).show();
			Log.e(getClass().getSimpleName(), e.getMessage());

			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean performSearchAlgorithm() {
		@SuppressWarnings("deprecation")
		String param1 = "searchstring=" + URLEncoder.encode(searchField.getText().toString());
		String param2 = "building=Alle";
		String param3 = "search=Suche+starten";

		String queryCss = "http://portal.mytum.de/layout.css";
		String queryExtraction = SERVICE_URL + "?" + param1 + "&" + param2 + "&" + param3;

		FileUtils.sendAsynchGetRequest(httpClient, this, queryCss, queryExtraction);
		return true;
	}

	@Override
	public void onSearchError(String message) {
		// TODO Auto-generated method stub
	}
}