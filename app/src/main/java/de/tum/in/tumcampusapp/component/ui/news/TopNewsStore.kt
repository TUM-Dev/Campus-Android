package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.SharedPreferences
import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsAlert
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import javax.inject.Inject

interface TopNewsStore {

    fun isEnabled(): Boolean

    fun setEnabled(isEnabled: Boolean)

    fun getNewsAlert(): NewsAlert?

    fun store(newsAlert: NewsAlert)

}

class RealTopNewsStore @Inject constructor(
        private val sharedPrefs: SharedPreferences
) : TopNewsStore {

    override fun isEnabled(): Boolean {
        return sharedPrefs.getBoolean(CardManager.SHOW_TOP_NEWS, true)
    }

    override fun setEnabled(isEnabled: Boolean) {
        sharedPrefs.edit().putBoolean(CardManager.SHOW_TOP_NEWS, true).apply()
    }

    override fun getNewsAlert(): NewsAlert? {
        val showTopNews = sharedPrefs.getBoolean(CardManager.SHOW_TOP_NEWS, true)

        val displayUntil = sharedPrefs.getString(Const.NEWS_ALERT_SHOW_UNTIL, "")
        val until = DateTimeUtils.parseIsoDateWithMillis(displayUntil)

        if (until == null || until.isBeforeNow || showTopNews.not()) {
            return null
        }

        val link = sharedPrefs.getString(Const.NEWS_ALERT_LINK, "")
        val imageUrl = sharedPrefs.getString(Const.NEWS_ALERT_IMAGE, "")
        return NewsAlert(imageUrl, link, displayUntil)
    }

    override fun store(newsAlert: NewsAlert) {
        val oldShowUntil = sharedPrefs.getString(Const.NEWS_ALERT_SHOW_UNTIL, "")
        val oldImage = sharedPrefs.getString(Const.NEWS_ALERT_IMAGE, "")

        sharedPrefs.edit().apply {
            putString(Const.NEWS_ALERT_IMAGE, newsAlert.url)
            putString(Const.NEWS_ALERT_LINK, newsAlert.link)
        }.apply()

        // there is a NewsAlert update if the image link or the date changed
        // --> Card should be displayed again
        val update = oldShowUntil != newsAlert.displayUntil || oldImage != newsAlert.url
        if (update) {
            sharedPrefs.edit().putBoolean(CardManager.SHOW_TOP_NEWS, true).apply()
        }

        sharedPrefs.edit().putString(Const.NEWS_ALERT_SHOW_UNTIL, newsAlert.displayUntil).apply()
    }

}
