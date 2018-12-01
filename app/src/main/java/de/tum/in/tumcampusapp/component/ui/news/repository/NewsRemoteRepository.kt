package de.tum.`in`.tumcampusapp.component.ui.news.repository

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.sync.SyncManager
import org.joda.time.DateTime
import java.io.IOException
import javax.inject.Inject

class NewsRemoteRepository @Inject constructor(
        private val localRepository: NewsLocalRepository,
        private val syncManager: SyncManager,
        private val tumCabeClient: TUMCabeClient,
        private val newsController: NewsController // TODO(thellmund) Replace NewsController with some NewsNotificationPresenter or similar
) {

    fun downloadFromExternal(force: Boolean) {
        if (!force && !syncManager.needSync(this, TIME_TO_SYNC)) {
            return
        }

        val latestNews = localRepository.getLast()
        val latestNewsDate = latestNews?.date ?: DateTime.now()

        // Delete all too old items
        localRepository.cleanUp()

        // Load all news sources
        try {
            val sources = tumCabeClient.newsSources
            localRepository.insertSources(sources)
        } catch (e: IOException) {
            Utils.log(e)
            return
        }

        // Load all news since the last sync
        try {
            val news = tumCabeClient.getNews(localRepository.getLastId())
            localRepository.insertNews(news)
            newsController.showNewsNotification(news, latestNewsDate)
        } catch (e: IOException) {
            Utils.log(e)
            return
        }

        // Finish sync
        syncManager.replaceIntoDb(this)
    }

    companion object {
        private const val TIME_TO_SYNC = 86400
    }

}
