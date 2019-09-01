package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.SharedPreferences
import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsAlert
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * This interface defines all possible operations on a persistent store for [NewsAlert] objects.
 * It's implemented by [RealTopNewsStore].
 */
interface TopNewsStore {

    /**
     * Returns whether the user has enabled the display of top-news cards in overview screen.
     *
     * @return True if top-news cards should be displayed
     */
    fun isEnabled(): Boolean

    /**
     * Sets whether top-news cards should be displayed in overview screen.
     *
     * @param isEnabled Whether top-news cards should be displayed
     */
    fun setEnabled(isEnabled: Boolean)

    /**
     * Returns the most recent [NewsAlert], or null if none is stored.
     *
     * @return The [NewsAlert] or null
     */
    fun getNewsAlert(): NewsAlert?

    /**
     * Stores the provided [NewsAlert] in the persistent store. It can later be retrieved via
     * [getNewsAlert].
     *
     * @param newsAlert The [NewsAlert] to be stored
     */
    fun store(newsAlert: NewsAlert)
}

class RealTopNewsStore @Inject constructor(
    private val sharedPrefs: SharedPreferences
) : TopNewsStore {

    override fun isEnabled(): Boolean {
        return sharedPrefs.getBoolean(CardManager.SHOW_TOP_NEWS, false)
    }

    override fun setEnabled(isEnabled: Boolean) {
        sharedPrefs.edit().putBoolean(CardManager.SHOW_TOP_NEWS, isEnabled).apply()
    }

    override fun getNewsAlert(): NewsAlert? {
        if (!isEnabled()) {
            return null
        }

        val displayUntil = sharedPrefs.getString(Const.NEWS_ALERT_SHOW_UNTIL, "")
        val until: DateTime? = DateTimeUtils.parseIsoDateWithMillis(displayUntil)
        if (until == null || until.isBeforeNow) {
            return null
        }

        val link = sharedPrefs.getString(Const.NEWS_ALERT_LINK, "")
        val imageUrl = sharedPrefs.getString(Const.NEWS_ALERT_IMAGE, "")
        return NewsAlert(imageUrl, link, displayUntil)
    }

    override fun store(newsAlert: NewsAlert) {
        if (newsAlert.displayUntil.isBlank()) {
            setEnabled(false)
            return
        }

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
            setEnabled(true)
        }

        sharedPrefs.edit().putString(Const.NEWS_ALERT_SHOW_UNTIL, newsAlert.displayUntil).apply()
    }
}
