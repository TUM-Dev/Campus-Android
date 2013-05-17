package de.tum.in.tumcampusapp.activities;

import java.io.File;
import java.net.URLEncoder;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.FileUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Activity to show a convenience interface for using the MyTUM room finder.
 * 
 * @author Vincenz Doelle
 */
public class RoomfinderActivity extends Activity implements OnEditorActionListener {

	// the URLs of the MyTUM roomfinder web service
	private final String SERVICE_BASE_URL = "http://portal.mytum.de/campus/roomfinder/";
	private final String SERVICE_URL = SERVICE_BASE_URL + "search_room_results";

	// HTTP client for sending requests to MyTUM roomfinder
	private DefaultHttpClient httpClient;

	// widgets
	private EditText etSearch;
	private WebView webView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_roomfinder);

		httpClient = new DefaultHttpClient();

		etSearch = (EditText) findViewById(R.id.etSearch);
		etSearch.setOnEditorActionListener(this);

		webView = Utils.getDefaultWebView(this, R.id.wvResults);

	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (etSearch.getText().length() < 3) {
			Utils.showLongCenteredToast(this, getString(R.string.please_insert_at_least_three_chars));
			return false;
		}

		Utils.hideKeyboard(this, etSearch);

		if (!Utils.isConnected(this)) {
			Utils.showLongCenteredToast(this, getString(R.string.no_internet_connection));
			return false;
		}

		// fetch css styles and build HTML document together with results
		String text = Utils.buildHTMLDocument(
				FileUtils.sendGetRequest(httpClient, "http://portal.mytum.de/layout.css"), extractResultsFromURL());

		// write resulting document to temporary file on SD-card
		File file = null;
		try {
			file = FileUtils.getFileOnSD(Const.ROOMFINDER, "tmp.html");

			FileUtils.writeFile(file, text);

			// get image and save it in the same folder as the document
			FileUtils.getFileFromURL(httpClient, SERVICE_BASE_URL + "/default.gif",
					FileUtils.getFileOnSD(Const.ROOMFINDER, "default.gif"));

			webView.loadUrl("file://" + file.getPath());

			return true;

		} catch (Exception e) {
			Utils.showLongCenteredToast(this, getString(R.string.no_sd_card));
			Log.d("EXCEPTION", e.getMessage());
		}

		return false;

	}

	/**
	 * Extract the results from the URL's document.
	 * 
	 * @return The extracted results.
	 */
	private String extractResultsFromURL() {
		String param1 = "searchstring=" + URLEncoder.encode(etSearch.getText().toString());
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
}