package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.news.repository.NewsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardsProvider
import de.tum.`in`.tumcampusapp.component.ui.tufilm.FilmCard
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Utils
import java.util.*
import javax.inject.Inject

class NewsCardsProvider @Inject constructor(
        private val context: Context,
        private val database: TcaDb,
        private val newsLocalRepository: NewsLocalRepository
) : CardsProvider {

    override fun provideCards(cacheControl: CacheControl): List<Card> {
        val results = ArrayList<Card>()
        val sources = newsLocalRepository.getActiveSources(context)

        val news = if (Utils.getSettingBool(context, "card_news_latest_only", true)) {
            database.newsDao().getBySourcesLatest(sources.toTypedArray())
        } else {
            database.newsDao().getBySources(sources.toTypedArray())
        }

        for (n in news) {
            val card: NewsCard
            if (n.isFilm) {
                card = FilmCard(context)
            } else {
                card = NewsCard(context)
            }

            card.setNews(n)
            card.getIfShowOnStart()?.let {
                results.add(it)
            }
        }

        return results
    }

}
