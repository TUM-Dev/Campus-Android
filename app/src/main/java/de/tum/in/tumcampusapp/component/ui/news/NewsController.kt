package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.notifications.ProvidesNotifications
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsSources
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.ProvidesCard
import de.tum.`in`.tumcampusapp.component.ui.tufilm.FilmCard
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.sync.SyncManager
import org.joda.time.DateTime
import java.io.IOException
import javax.inject.Inject

private const val TIME_TO_SYNC = 86400

class NewsController @Inject constructor(
    private val context: Context
) : ProvidesCard, ProvidesNotifications {

    private val newsDao = TcaDb.getInstance(context).newsDao()
    private val newsSourcesDao = TcaDb.getInstance(context).newsSourcesDao()

    /**
     * Get the index of the newest item that is older than 'now'
     *
     * @return index of the newest item that is older than 'now' - 1
     */
    val todayIndex: Int
        get() {
            val selectedNewspread = Utils.getSetting(context, "news_newspread", "7").toInt()
            val news = newsDao.getNewer(selectedNewspread)
            return if (news.isEmpty()) 0 else news.size - 1
        }

    val newsSources: List<NewsSources>
        get() {
            val selectedNewspread = Utils.getSetting(context, "news_newspread", "7")
            return newsSourcesDao.getNewsSources(selectedNewspread)
        }

    /**
     * Gather all sources that should be displayed
     */
    val activeSources: Collection<Int>
        get() {
            return newsSources
                .map { it.id }
                .filter { Utils.getSettingBool(context, "card_news_source_$it", true) }
        }

    /**
     * Download news from external interface (JSON)
     *
     * @param force True to force download over normal sync period, else false
     */
    fun downloadFromExternal(force: CacheControl) {
        val sync = SyncManager(context)
        if (force === CacheControl.USE_CACHE && !sync.needSync(this, TIME_TO_SYNC)) {
            return
        }

        val latestNews = newsDao.last
        val latestNewsDate = latestNews?.date ?: DateTime.now()

        // Delete all too old items
        newsDao.cleanUp()

        val api = TUMCabeClient.getInstance(context)

        // Load all news sources
        try {
            val sources = api.getNewsSources()
            if (sources != null) {
                newsSourcesDao.insert(sources)
            }
        } catch (e: IOException) {
            Utils.log(e)
            return
        }

        // Load all news since the last sync
        try {
            val news = api.getNews(getLastId())
            if (news != null) {
                newsDao.insert(news)
            }
            showNewsNotification(news, latestNewsDate)
        } catch (e: IOException) {
            Utils.log(e)
            return
        }

        // Finish sync
        sync.replaceIntoDb(this)
    }

    private fun showNewsNotification(news: List<News>, latestNewsDate: DateTime) {
        if (!hasNotificationsEnabled()) {
            return
        }

        val newNews = news.filter { it.date.isAfter(latestNewsDate) }

        if (newNews.isEmpty()) {
            return
        }

        val provider = NewsNotificationProvider(context, newNews)
        val notification = provider.buildNotification()

        if (notification != null) {
            val scheduler = NotificationScheduler(context)
            scheduler.schedule(notification)
        }
    }

    /**
     * Get all news from the database
     *
     * @return List of News
     */
    fun getAllFromDb(context: Context): List<News> {
        val selectedNewspread = Utils.getSetting(this.context, "news_newspread", "7").toInt()

        val ids = newsSources
            .map { it.id }
            .filter { Utils.getSettingBool(context, "news_source_$it", it <= 7) }

        return newsDao.getAll(ids.toTypedArray(), selectedNewspread)
    }

    private fun getLastId(): String {
        return newsDao.last?.id ?: ""
    }

    fun setDismissed(id: String, d: Int) {
        newsDao.setDismissed(d.toString(), id)
    }

    override fun getCards(cacheControl: CacheControl): List<Card> {
        val news = if (Utils.getSettingBool(context, "card_news_latest_only", true)) {
            newsDao.getBySourcesLatest(activeSources.toTypedArray())
        } else {
            newsDao.getBySources(activeSources.toTypedArray())
        }

        return news
            .map { item -> if (item.isFilm) FilmCard(context, item) else NewsCard(context, item) }
            .mapNotNull { it.getIfShowOnStart() }
    }

    override fun hasNotificationsEnabled(): Boolean {
        return Utils.getSettingBool(context, "card_news_phone", false)
    }
}
