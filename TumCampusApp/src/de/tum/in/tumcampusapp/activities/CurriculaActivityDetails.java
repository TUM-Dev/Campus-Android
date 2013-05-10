package de.tum.in.tumcampusapp.activities;

import java.io.File;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.FileUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Activity to fetch and display the curricula of different programs.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
public class CurriculaActivityDetails extends Activity {

	// fetch information in a background task and show progress dialog in
	// meantime
	final AsyncTask<Object, Void, File> backgroundTask = new AsyncTask<Object, Void, File>() {

		@Override
		protected File doInBackground(Object... params) {
			fetchCurriculum((String) params[0], (File) params[1]);
			return (File) params[1];
		}

		@Override
		protected void onPostExecute(File result) {
			openFile(result);
			progressLayout.setVisibility(View.GONE);
		}
	};

	private WebView browser;
	private RelativeLayout errorLayout;
	/** Http client to fetch the curricula data */
	private DefaultHttpClient httpClient;
	private RelativeLayout progressLayout;

	/**
	 * Extract the results from a document fetched from the given URL.
	 * 
	 * @param url
	 *            URL pointing to a document where the results are extracted
	 *            from.
	 * @return The results.
	 */
	private String extractResultsFromURL(String url) {
		String text = FileUtils.sendGetRequest(httpClient, url);

		if (text == null) {
			return getString(R.string.something_wrong);
		}
		return Utils.cutText(text, "<!--TYPO3SEARCH_begin-->", "<!--TYPO3SEARCH_end-->");
	}

	/**
	 * Fetches the curriculum document and extracts all relevant information.
	 * 
	 * @param url
	 *            URL of the curriculum document
	 * @param targetFile
	 *            Target where the results should be written to
	 */
	private void fetchCurriculum(String url, File targetFile) {
		String text = Utils.buildHTMLDocument(FileUtils.sendGetRequest(httpClient, "http://www.in.tum.de/fileadmin/_src/add.css"),
				"<div id=\"maincontent\"><div class=\"inner\">" + extractResultsFromURL(url) + "</div></div>");

		text = text.replace("href=\"fuer-studierende-der-tum", "href=\"http://www.in.tum.de/fuer-studierende-der-tum");

		FileUtils.writeFile(targetFile, text);
	}

	/**
	 * Downloads the curricula data, parses the relevant content, adds the
	 * corresponding css information and creates a new html document.
	 * 
	 * @param name
	 *            The name of the curriculum as displayed in the list.
	 * @param url
	 *            The url of the curriculum to be downloaded.
	 */
	private void getCurriculum(String name, final String url) {

		String filename = FileUtils.getFilename(name, ".html");

		File file = null;
		try {
			file = FileUtils.getFileOnSD(Const.CURRICULA, filename);
		} catch (Exception e) {
			progressLayout.setVisibility(View.GONE);
			errorLayout.setVisibility(View.VISIBLE);
			Log.e("EXCEPTION", e.getMessage());
		}

		if (file == null) {
			return; // cannot work without target file
		}

		// if file does not exist download it again
		if (!file.exists()) {
			if (!Utils.isConnected(this)) {
				Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
				progressLayout.setVisibility(View.GONE);
				errorLayout.setVisibility(View.VISIBLE);
				return;
			}
			backgroundTask.execute(url, file);

		} else {
			openFile(file);
			progressLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_curricula_details);

		httpClient = new DefaultHttpClient();

		browser = (WebView) findViewById(R.id.activity_curricula_web_view);
		progressLayout = (RelativeLayout) findViewById(R.id.activity_curricula_progress_layout);
		errorLayout = (RelativeLayout) findViewById(R.id.activity_curricula_error_layout);

		browser.getSettings().setBuiltInZoomControls(true);
		browser.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
		browser.getSettings().setUseWideViewPort(true);

		String url = getIntent().getExtras().getString(CurriculaActivity.URL);
		String name = getIntent().getExtras().getString(CurriculaActivity.NAME);

		setTitle(getTitle() + " for " + name);

		progressLayout.setVisibility(View.VISIBLE);
		getCurriculum(name, url);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		backgroundTask.cancel(true);
	}

	/**
	 * Opens a local file.
	 * 
	 * @param file
	 *            File to be opened.
	 */
	private void openFile(File file) {
		if (file == null) {
			return;
		}
		browser.loadUrl("file://" + file.getPath());
	}
}
