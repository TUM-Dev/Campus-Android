package de.tum.`in`.tumcampusapp.component.ui.news.repository

import android.annotation.SuppressLint
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsStore
import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsAlert
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TopNewsRemoteRepository @Inject constructor(
        private val topNewsStore: TopNewsStore,
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
        topNewsStore.store(newsAlert)
    }

}
