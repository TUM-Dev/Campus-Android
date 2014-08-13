package de.tum.in.tumcampus.activities;

import java.io.File;

import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.FileUtils;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Activity to display the curricula details of different programs.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
public class CurriculaDetailsActivity extends ActionBarActivity {

	// Fetch information in a background task and show progress dialog in meantime
	final AsyncTask<Object, Void, File> backgroundTask = new AsyncTask<Object, Void, File>() {

		@Override
		protected File doInBackground(Object... params) {
			CurriculaDetailsActivity.this.fetchCurriculum((String) params[0], (File) params[1]);
			return (File) params[1];
		}

		@Override
		protected void onPostExecute(File result) {
			CurriculaDetailsActivity.this.openFile(result);
			CurriculaDetailsActivity.this.progressLayout.setVisibility(View.GONE);
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
	 *            URL pointing to a document where the results are extracted from.
	 * @return The results.
	 */
	private String extractResultsFromURL(String url) {
		String text = FileUtils.sendGetRequest(this.httpClient, url);

		if (text == null) {
			return this.getString(R.string.something_wrong);
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
		String text = Utils.buildHTMLDocument(FileUtils.sendGetRequest(this.httpClient, "http://www.in.tum.de/fileadmin/_src/add.css"),
				"<div id=\"maincontent\"><div class=\"inner\">" + this.extractResultsFromURL(url) + "</div></div>");

		text = text.replace("href=\"fuer-studierende-der-tum", "href=\"http://www.in.tum.de/fuer-studierende-der-tum");

		FileUtils.writeFile(targetFile, text);
	}

	/**
	 * Downloads the curricula data, parses the relevant content, adds the corresponding css information and creates a new html document.
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
			this.progressLayout.setVisibility(View.GONE);
			this.errorLayout.setVisibility(View.VISIBLE);
			Log.e("EXCEPTION", e.getMessage());
		}

		if (file == null) {
			// cannot work without target file
			return;
		}

		// if file does not exist download it again
		if (!file.exists()) {
			if (!Utils.isConnected(this)) {
				Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
				this.progressLayout.setVisibility(View.GONE);
				this.errorLayout.setVisibility(View.VISIBLE);
				return;
			}
			this.backgroundTask.execute(url, file);

		} else {
			this.openFile(file);
			this.progressLayout.setVisibility(View.GONE);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_curriculadetails);

		// TODO export this to the generic app structure
		this.httpClient = new DefaultHttpClient();

		this.browser = (WebView) this.findViewById(R.id.activity_curricula_web_view);
		this.progressLayout = (RelativeLayout) this.findViewById(R.id.progress_layout);
		this.errorLayout = (RelativeLayout) this.findViewById(R.id.error_layout);

		this.browser.getSettings().setBuiltInZoomControls(true);
		this.browser.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
		this.browser.getSettings().setUseWideViewPort(true);

		String url = this.getIntent().getExtras().getString(CurriculaActivity.URL);
		String name = this.getIntent().getExtras().getString(CurriculaActivity.NAME);

		this.setTitle(name);

		this.progressLayout.setVisibility(View.VISIBLE);
		this.getCurriculum(name, url);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.backgroundTask.cancel(true);
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
		this.browser.loadUrl("file://" + file.getPath());
	}
}
