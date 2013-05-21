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

	/**
	 * Extract the results from the URL's document.
	 * 
	 * @return The extracted results.
	 */
	private String extractResultsFromURL() {
		String param1 = "searchstring=" + URLEncoder.encode(searchField.getText().toString());
		String param2 = "building=Alle";
		String param3 = "search=Suche+starten";

		String query = SERVICE_URL + "?" + param1 + "&" + param2 + "&" + param3;

		// download file
		String text = FileUtils.sendGetRequest(httpClient, query);

		if (text == null) {
			return getString(R.string.something_wrong);
		}

		text = Utils.cutText(text, "<div id=\"maincontentwrapper\">", "<div class=\"documentActions\">");

		// fit all links
		text = text.replace("<a href=\"search_room_form\">", "<a href=\"" + SERVICE_BASE_URL + "search_room_form\">");
		text = text.replace("<a href=\"search_room_results", "<a href=\"" + SERVICE_BASE_URL + "search_room_results");

		return text;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		httpClient = new DefaultHttpClient();
		webView = Utils.getDefaultWebView(this, R.id.webview_results);
	}

	@Override
	public boolean performSearchAlgorithm() {
		FileUtils.sendAsynchGetRequest(httpClient, "http://portal.mytum.de/layout.css", this);
		return true;
	}

	@Override
	public void onSearchResult(String result) {
		// TODO This is also asynch!
		String text = Utils.buildHTMLDocument(result,  extractResultsFromURL());
		// write resulting document to temporary file on SD-card
		File file = null;
		try {
			file = FileUtils.getFileOnSD(Const.ROOMFINDER, "tmp.html");
			FileUtils.writeFile(file, text);

			// get image and save it in the same folder as the document
			FileUtils.getFileFromURL(httpClient, SERVICE_BASE_URL + "/default.gif", FileUtils.getFileOnSD(Const.ROOMFINDER, "default.gif"));

			webView.loadUrl("file://" + file.getPath());
			
			errorLayout.setVisibility(View.GONE);
			progressLayout.setVisibility(View.GONE);
		} catch (Exception e) {
			Toast.makeText(this, R.string.no_sd_card, Toast.LENGTH_SHORT).show();
			Log.e(getClass().getSimpleName(), e.getMessage());
			
			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
		}
	}
}