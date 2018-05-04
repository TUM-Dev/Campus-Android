package de.tum.`in`.tumcampusapp.component.ui.curricula

import android.os.Bundle
import com.google.common.base.Optional
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground
import de.tum.`in`.tumcampusapp.utils.CacheManager
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.activity_curriculadetails.*

/**
 * Activity to display the curricula details of different programs.
 *
 * NEEDS: CurriculaActivity.URL set in incoming bundle (url to load study plan from)
 * CurriculaActivity.NAME set in incoming bundle (name of the study program)
 */
class CurriculaDetailsActivity : ActivityForLoadingInBackground<String, Optional<String>>(R.layout.activity_curriculadetails) {

    private lateinit var net: NetUtils

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        net = NetUtils(this)
        activity_curricula_web_view.settings.builtInZoomControls = true

        if(intent.extras == null) {
            this.finish()
        }
        val url = intent.extras.getString(CurriculaActivity.URL)
        val name = intent.extras.getString(CurriculaActivity.NAME)

        title = name
        startLoading(url)
    }

    /**
     * Fetch information in a background task and show progress dialog in meantime
     */
    override fun onLoadInBackground(vararg params: String): Optional<String> {
        return fetchCurriculum(params[0])
    }

    /**
     * Fetches the curriculum document and extracts all relevant information.
     *
     * @param url URL of the curriculum document
     */
    private fun fetchCurriculum(url: String): Optional<String> {
        val results = extractResultsFromURL(url)
        val css = net.downloadStringAndCache("http://www.in.tum.de/fileadmin/_src/add.css", CacheManager.VALIDITY_ONE_MONTH, false)
        if (!results.isPresent || !css.isPresent) {
            return Optional.absent()
        }
        val text = Utils.buildHTMLDocument(css.get(), "<div id=\"maincontent\"><div class=\"inner\">" + results.get() + "</div></div>")
        return Optional.of(text.replace("href=\"fuer-studierende-der-tum", "href=\"http://www.in.tum.de/fuer-studierende-der-tum"))
    }

    /**
     * Extract the results from a document fetched from the given URL.
     *
     * @param url URL pointing to a document where the results are extracted from.
     * @return The results.
     */
    private fun extractResultsFromURL(url: String): Optional<String> {
        val text = net.downloadStringAndCache(url, CacheManager.VALIDITY_ONE_MONTH, false)

        if (text.isPresent) {
            return Optional.of(Utils.cutText(text.get(), "<!--TYPO3SEARCH_begin-->", "<!--TYPO3SEARCH_end-->"))
        }
        if (NetUtils.isConnected(this)) {
            showError(R.string.something_wrong)
        } else {
            showNoInternetLayout()
        }
        return Optional.absent()
    }

    /**
     * When html data is loaded show it in webView
     *
     * @param result File
     */
    override fun onLoadFinished(result: Optional<String>) {
        if (result.isPresent) {
            activity_curricula_web_view.loadData(result.get(), "text/html; charset=UTF-8", null)
            showLoadingEnded()
        }
    }
}
