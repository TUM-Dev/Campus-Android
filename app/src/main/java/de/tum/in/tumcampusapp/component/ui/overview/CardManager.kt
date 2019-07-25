package de.tum.`in`.tumcampusapp.component.ui.overview

import android.content.Context
import android.preference.PreferenceManager
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const.CARD_POSITION_PREFERENCE_SUFFIX
import de.tum.`in`.tumcampusapp.utils.Const.DISCARD_SETTINGS_START
import de.tum.`in`.tumcampusapp.utils.Utils

/**
 * Card manager, manages inserting, dismissing, updating and displaying of cards
 */
object CardManager {

    const val SHOW_SUPPORT = "show_support"
    const val SHOW_LOGIN = "show_login"
    const val SHOW_TOP_NEWS = "show_top_news"

    /**
     * Card typ constants
     */
    const val CARD_CAFETERIA = R.layout.card_cafeteria_menu
    const val CARD_TUITION_FEE = R.layout.card_tuition_fees
    const val CARD_NEXT_LECTURE = R.layout.card_next_lecture_item
    const val CARD_RESTORE = R.layout.card_restore
    const val CARD_NO_INTERNET = R.layout.card_no_internet
    const val CARD_MVV = R.layout.card_mvv
    const val CARD_NEWS = R.layout.card_news_item
    const val CARD_NEWS_FILM = R.layout.card_news_film_item
    const val CARD_EDUROAM = R.layout.card_eduroam
    const val CARD_CHAT = R.layout.card_chat_messages
    const val CARD_SUPPORT = R.layout.card_support
    const val CARD_LOGIN = R.layout.card_login_prompt
    const val CARD_EDUROAM_FIX = R.layout.card_eduroam_fix
    const val CARD_TOP_NEWS = R.layout.card_top_news
    const val CARD_EVENT = R.layout.card_events_item
    const val CARD_UPDATE_NOTE = R.layout.card_update_note

    /**
     * Resets dismiss settings for all cards
     */
    fun restoreCards(context: Context) {
        context.getSharedPreferences(DISCARD_SETTINGS_START, 0)
                .edit()
                .clear()
                .apply()

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
