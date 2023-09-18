package de.tum.`in`.tumcampusapp.component.ui.overview

import android.content.Context
import androidx.preference.PreferenceManager
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const.CARD_POSITION_PREFERENCE_SUFFIX
import de.tum.`in`.tumcampusapp.utils.Utils

/**
 * Card manager, manages inserting, dismissing, updating and displaying of cards
 */
object CardManager {

    const val SHOW_TOP_NEWS = "show_top_news"

    /**
     * Card typ constants
     */
    enum class CardTypes(val id: Int, val showCardPreferenceStringRes: Int) {
        CAFETERIA(R.layout.card_cafeteria_menu, R.string.cafeteria_default_sharedpref_shown),
        TUITION_FEE(R.layout.card_tuition_fees, R.string.tuition_fee_default_sharedpref_shown),
        NEXT_LECTURE(R.layout.card_next_lecture_item, R.string.next_lecture_default_sharedpref_shown),
        RESTORE(R.layout.card_restore, R.string.restore_default_sharedpref_shown),
        NO_INTERNET(R.layout.card_no_internet, R.string.no_internet_default_sharedpref_shown),
        MVV(R.layout.card_mvv, R.string.mvv_default_sharedpref_shown),
        NEWS(R.layout.card_news_item, R.string.news_default_sharedpref_shown),
        NEWS_FILM(R.layout.card_news_film_item, R.string.news_film_default_sharedpref_shown),
        EDUROAM(R.layout.card_eduroam, R.string.eduroam_default_sharedpref_shown),
        SUPPORT(R.layout.card_support, R.string.support_default_sharedpref_shown),
        LOGIN(R.layout.card_login_prompt, R.string.login_default_sharedpref_shown),
        EDUROAM_FIX(R.layout.card_eduroam_fix, R.string.eduroam_fix_default_sharedpref_shown),
        TOP_NEWS(R.layout.card_top_news, R.string.top_news_default_sharedpref_shown),
        EVENT(R.layout.card_events_item, R.string.event_default_sharedpref_shown),
        UPDATE_NOTE(R.layout.card_update_note, R.string.update_note_default_sharedpref_shown)
    }

    /**
     * Resets dismiss settings for all cards
     */
    fun restoreCards(context: Context) {
        CardTypes.values().forEach {
            context.getSharedPreferences("CardPref$it", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }

        TcaDb.getInstance(context)
            .newsDao()
            .restoreAllNews()

        Utils.setSetting(context, SHOW_TOP_NEWS, true)
        restoreCardPositions(context)
    }

    private fun restoreCardPositions(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()

        for (s in preferences.all.keys) {
            if (s.endsWith(CARD_POSITION_PREFERENCE_SUFFIX)) {
                editor.remove(s)
            }
        }
        editor.apply()
    }
}
