package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.news.model.News

class NewsAdapter(
    context: Context,
    private val news: List<News>
) : RecyclerView.Adapter<NewsViewHolder>() {

    private val newsInflater = NewsInflater(context)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewsViewHolder = newsInflater.onCreateNewsView(parent, viewType, false)

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        newsInflater.onBindNewsView(holder, news[position])
    }

    override fun getItemViewType(position: Int): Int {
        val newsItem = news[position]

        return if (newsItem.isFilm) {
            R.layout.card_news_film_item
        } else {
            R.layout.card_news_item
        }
    }

    override fun getItemCount() = news.size

}
