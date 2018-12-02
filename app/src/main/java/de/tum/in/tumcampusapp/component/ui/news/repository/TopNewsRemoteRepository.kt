package de.tum.`in`.tumcampusapp.component.ui.news.repository

import android.annotation.SuppressLint
import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsAlert
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class TopNewsRemoteRepository(
        private val context: Context,
        private val tumCabeClient: TUMCabeClient
) {

    /**
     * Downloads the NewsAlert and stores it in the sharedPreferences
     */
    @SuppressLint("CheckResult")
    fun fetchNewsAlert() {
        tumCabeClient.newsAlert
                .subscribeOn(Schedulers.io())
                .subscribe(this::onTopNewsDownloaded, Utils::log)
    }

    private fun onTopNewsDownloaded(newsAlert: NewsAlert) {
        Utils.setSetting(context, Const.NEWS_ALERT_IMAGE, newsAlert.url)
        Utils.setSetting(context, Const.NEWS_ALERT_LINK, newsAlert.link)

        val oldShowUntil = Utils.getSetting(context, Const.NEWS_ALERT_SHOW_UNTIL, "")
        val oldImage = Utils.getSetting(context, Const.NEWS_ALERT_IMAGE, "");

        // there is a NewsAlert update if the image link or the date changed
        // --> Card should be displayed again
        val update = oldShowUntil != newsAlert.displayUntil || oldImage != newsAlert.url
        if (update) {
            Utils.setSetting(context, CardManager.SHOW_TOP_NEWS, true)
        }
        Utils.setSetting(context, Const.NEWS_ALERT_SHOW_UNTIL, newsAlert.displayUntil)
    }

    fun getNewsAlert(): Observable<NewsAlert> = tumCabeClient.newsAlert

}
