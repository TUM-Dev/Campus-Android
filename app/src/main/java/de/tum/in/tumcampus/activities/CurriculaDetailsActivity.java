package de.tum.in.tumcampus.activities;

import android.os.Bundle;
import android.webkit.WebView;

import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.FileUtils;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Activity to display the curricula details of different programs.
 *
 * NEEDS: CurriculaActivity.URL set in incoming bundle (url to load study plan from)
 *        CurriculaActivity.NAME set in incoming bundle (name of the study program)
 */
public class CurriculaDetailsActivity extends ActivityForLoadingInBackground<Object,File> {

	private WebView browser;

	/** Http client to fetch the curricula data */
	private DefaultHttpClient httpClient;

    public CurriculaDetailsActivity() {
        super(R.layout.activity_curriculadetails);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        httpClient = new DefaultHttpClient();

        browser = (WebView) this.findViewById(R.id.activity_curricula_web_view);
        browser.getSettings().setBuiltInZoomControls(true);

        String url = getIntent().getExtras().getString(CurriculaActivity.URL);
        String name = getIntent().getExtras().getString(CurriculaActivity.NAME);

        setTitle(name);
        getCurriculum(name, url);
    }

    /**
	 * Extract the results from a document fetched from the given URL.
	 * 
	 * @param url URL pointing to a document where the results are extracted from.
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
	 * @param url URL of the curriculum document
	 * @param targetFile Target where the results should be written to
	 */
	private void fetchCurriculum(String url, File targetFile) {
		String text = Utils.buildHTMLDocument(FileUtils.sendGetRequest(httpClient, "http://www.in.tum.de/fileadmin/_src/add.css"),
				"<div id=\"maincontent\"><div class=\"inner\">" + extractResultsFromURL(url) + "</div></div>");

		text = text.replace("href=\"fuer-studierende-der-tum", "href=\"http://www.in.tum.de/fuer-studierende-der-tum");

		FileUtils.writeFile(targetFile, text);
	}

	/**
	 * Downloads the curricula data, parses the relevant content, adds the corresponding css information and creates a new html document.
	 * 
	 * @param name The name of the curriculum as displayed in the list.
	 * @param url The url of the curriculum to be downloaded.
	 */
	private void getCurriculum(String name, final String url) {
		String filename = name.replace(" ", "_") + ".html";

		File file = null;
		try {
			file = FileUtils.getFileOnSD(Const.CURRICULA, filename);
		} catch (Exception e) {
            showError(e.getMessage());
		}

		if (file == null) {
			// cannot work without target file
			return;
		}

		// if file does not exist download it again
		if (!file.exists()) {
			if (!Utils.isConnected(this)) {
                showError(R.string.no_internet_connection);
				return;
			}
			startLoading(url, file);
		} else {
			openFile(file);
		}
	}

    /**
     * Fetch information in a background task and show progress dialog in meantime
     */
    @Override
    protected File onLoadInBackground(Object... params) {
        fetchCurriculum((String) params[0], (File) params[1]);
        return (File) params[1];
    }

    /**
     * When file is available, open it
     * @param result File
     */
    @Override
    protected void onLoadFinished(File result) {
        openFile(result);
        showLoadingEnded();
    }

	/**
	 * Opens a local file.
	 * 
	 * @param file File to be opened.
	 */
	private void openFile(File file) {
		if (file == null) {
			return;
		}
		this.browser.loadUrl("file://" + file.getPath());
	}
}
