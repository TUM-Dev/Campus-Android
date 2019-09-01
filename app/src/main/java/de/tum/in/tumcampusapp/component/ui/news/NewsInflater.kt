package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.database.TcaDb

class NewsInflater(context: Context) {

    private val newsSourcesDao: NewsSourcesDao by lazy {
        TcaDb.getInstance(context).newsSourcesDao()
    }
    private val newsDao: NewsDao by lazy {
        TcaDb.getInstance(context).newsDao()
    }

    @JvmOverloads
    fun onCreateNewsView(
        parent: ViewGroup,
        layoutId: Int,
        showOptionsButton: Boolean = true,
        interactionListener: CardInteractionListener? = null
    ): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return NewsViewHolder(view, interactionListener, showOptionsButton)
    }

    fun onBindNewsView(viewHolder: NewsViewHolder, newsItem: News) {
        val newsSource = newsSourcesDao.getNewsSource(newsItem.src.toInt())
        val nrOfEvents = newsDao.hasEventAssociated(newsItem.id)
        val hasEvents = nrOfEvents != 0
        viewHolder.bind(newsItem, newsSource, hasEvents)
    }
}
