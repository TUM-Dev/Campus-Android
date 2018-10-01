package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.database.TcaDb

class NewsInflater(context: Context) {

    private val newsSourcesDao: NewsSourcesDao by lazy {
        TcaDb.getInstance(context).newsSourcesDao()
    }

    fun onCreateNewsView(parent: ViewGroup,
                         layoutId: Int, showOptionsButton: Boolean = true): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return NewsViewHolder(view, showOptionsButton)
    }

    fun onBindNewsView(viewHolder: NewsViewHolder, newsItem: News) {
        val newsSource = newsSourcesDao.getNewsSource(newsItem.src.toInt())
        viewHolder.bind(newsItem, newsSource)
    }

}
