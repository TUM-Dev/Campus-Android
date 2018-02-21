package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.webkit.WebView;

import com.google.common.base.Optional;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.CacheManager;

/**
 * Activity to display the curricula details of different programs.
 * <p/>
 * NEEDS: CurriculaActivity.URL set in incoming bundle (url to load study plan from)
 * CurriculaActivity.NAME set in incoming bundle (name of the study program)
 */
public class CurriculaDetailsActivity extends ActivityForLoadingInBackground<String, Optional<String>> {

    private WebView browser;
    private NetUtils net;

    public CurriculaDetailsActivity() {
        super(R.layout.activity_curriculadetails);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        net = new NetUtils(this);
        browser = findViewById(R.id.activity_curricula_web_view);
        browser.getSettings()
               .setBuiltInZoomControls(true);

        String url = getIntent().getExtras()
                                .getString(CurriculaActivity.URL);
        String name = getIntent().getExtras()
                                 .getString(CurriculaActivity.NAME);

        setTitle(name);
        startLoading(url);
    }

    /**
     * Fetch information in a background task and show progress dialog in meantime
     */
    @Override
    protected Optional<String> onLoadInBackground(String... params) {
        return fetchCurriculum(params[0]);
    }

    /**
     * Fetches the curriculum document and extracts all relevant information.
     *
     * @param url URL of the curriculum document
     */
    private Optional<String> fetchCurriculum(String url) {
        Optional<String> results = extractResultsFromURL(url);
        Optional<String> css = net.downloadStringAndCache("http://www.in.tum.de/fileadmin/_src/add.css", CacheManager.VALIDITY_ONE_MONTH, false);
        if (!results.isPresent() || !css.isPresent()) {
            return Optional.absent();
        }
        String text = Utils.buildHTMLDocument(css.get(), "<div id=\"maincontent\"><div class=\"inner\">" + results.get() + "</div></div>");
        return Optional.of(text.replace("href=\"fuer-studierende-der-tum", "href=\"http://www.in.tum.de/fuer-studierende-der-tum"));
    }

    /**
     * Extract the results from a document fetched from the given URL.
     *
     * @param url URL pointing to a document where the results are extracted from.
     * @return The results.
     */
    private Optional<String> extractResultsFromURL(String url) {
        Optional<String> text = net.downloadStringAndCache(url, CacheManager.VALIDITY_ONE_MONTH, false);

        if (text.isPresent()) {
            return Optional.of(Utils.cutText(text.get(), "<!--TYPO3SEARCH_begin-->", "<!--TYPO3SEARCH_end-->"));
        }
        if (NetUtils.isConnected(this)) {
            showError(R.string.something_wrong);
        } else {
            showNoInternetLayout();
        }
        return Optional.absent();
    }

    /**
     * When html data is loaded show it in webView
     *
     * @param result File
     */
    @Override
    protected void onLoadFinished(Optional<String> result) {
        if (result.isPresent()) {
            browser.loadData(result.get(), "text/html; charset=UTF-8", null);
            showLoadingEnded();
        }
    }
}
