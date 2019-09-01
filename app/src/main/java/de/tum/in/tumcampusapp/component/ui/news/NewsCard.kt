package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.Context
import android.content.SharedPreferences
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavDestination
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.utils.Utils
import org.joda.time.DateTime

/**
 * Card that shows selected news
 */
open class NewsCard @JvmOverloads constructor(
    context: Context,
    val news: News,
    type: Int = CardManager.CARD_NEWS
) : Card(type, context, "card_news") {

    override val optionsMenuResId: Int
        get() = R.menu.card_popup_menu

    val title: String
        get() = news.title

    val source: String
        get() = news.src

    val date: DateTime
        get() = news.date

    override fun getId(): Int {
        return news.id.toInt()
    }

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        super.updateViewHolder(viewHolder)
        val holder = viewHolder as NewsViewHolder
        mNewsInflater.onBindNewsView(holder, news)
    }

    override fun shouldShow(prefs: SharedPreferences): Boolean {
        return news.dismissed and 1 == 0
    }

    override fun getNavigationDestination(): NavDestination? {
        val url = news.link
        if (url.isEmpty()) {
            Utils.showToast(context, R.string.no_link_existing)
            return null
        }
        return NavDestination.Link(url)
    }

    override fun discard(editor: SharedPreferences.Editor) {
        val newsController = NewsController(context)
        newsController.setDismissed(news.id, news.dismissed or 1)
    }

    companion object {
        private lateinit var mNewsInflater: NewsInflater
        @JvmStatic
        fun inflateViewHolder(parent: ViewGroup, viewType: Int, interactionListener: CardInteractionListener): CardViewHolder {
            mNewsInflater = NewsInflater(parent.context)
            return mNewsInflater.onCreateNewsView(parent, viewType, true, interactionListener)
        }
    }
}
