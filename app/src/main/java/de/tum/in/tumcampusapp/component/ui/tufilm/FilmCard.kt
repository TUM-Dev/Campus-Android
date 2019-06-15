package de.tum.`in`.tumcampusapp.component.ui.tufilm

import android.content.Context
import android.os.Bundle

import de.tum.`in`.tumcampusapp.component.other.navigation.NavDestination
import de.tum.`in`.tumcampusapp.component.ui.news.NewsCard
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils

class FilmCard(context: Context, news: News) : NewsCard(CardManager.CARD_NEWS_FILM, context, news) {
    override fun getNavigationDestination(): NavDestination {
        val args = Bundle()
        args.putString(Const.KINO_DATE, DateTimeUtils.getDateTimeString(date))
        return NavDestination.Activity(KinoActivity::class.java, args)
    }
}
