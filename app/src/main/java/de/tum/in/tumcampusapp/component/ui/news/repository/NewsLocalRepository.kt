package de.tum.`in`.tumcampusapp.component.ui.news.repository

import android.content.Context
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsSources
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Utils
import java.util.*
import javax.inject.Inject

class NewsLocalRepository @Inject constructor(
        private val context: Context,
        private val database: TcaDb
) {

    fun getAll(): List<News> {
        val selectedNewspread = Utils.getSetting(this.context, "news_newspread", "7").toInt()
        val newsSources = getNewsSources()
        val newsSourceIds = ArrayList<Int>()
        for (newsSource in newsSources) {
            val id = newsSource.id
            val show = Utils.getSettingBool(context, "news_source_$id", id <= 7)
            if (show) {
                newsSourceIds.add(id)
            }
        }
        return database.newsDao().getAll(newsSourceIds.toTypedArray(), selectedNewspread)
    }

    fun insertSources(sources: List<NewsSources>) {
        database.newsSourcesDao().insert(sources)
    }

    fun insertNews(news: List<News>) {
        database.newsDao().insert(news)
    }

    fun getTodayIndex(): Int {
        val selectedNewspread = Integer.parseInt(Utils.getSetting(context, "news_newspread", "7"))
        val news = database.newsDao().getNewer(selectedNewspread)
        return if (news.isEmpty()) 0 else news.size - 1
    }

    fun getLastId(): String {
        val last = database.newsDao().last
        return last?.id ?: ""
    }

    fun getLast(): News? {
        return database.newsDao().last
    }

    fun cleanUp() {
        database.newsDao().cleanUp()
    }

    fun setDismissed(id: String, d: Int) {
        database.newsDao().setDismissed(d.toString(), id)
    }

    fun getActiveSources(context: Context): List<Int> {
        val sources = ArrayList<Int>()
        val newsSources = getNewsSources()
        for (newsSource in newsSources) {
            val id = newsSource.id
            if (Utils.getSettingBool(context, "card_news_source_$id", true)) {
                sources.add(id)
            }
        }
        return sources
    }

    fun getNewsSources(): List<NewsSources> {
        val selectedNewspread = Utils.getSetting(context, "news_newspread", "7")
        return database.newsSourcesDao().getNewsSources(selectedNewspread)
    }

}
