package de.tum.`in`.tumcampusapp.component.ui.overview

import android.content.Context
import android.preference.PreferenceManager
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
    enum class CardTypes(val id: Int) {
        CAFETERIA(R.layout.card_cafeteria_menu),
        TUITION_FEE(R.layout.card_tuition_fees),
        NEXT_LECTURE(R.layout.card_next_lecture_item),
        RESTORE(R.layout.card_restore),
        NO_INTERNET(R.layout.card_no_internet),
        MVV(R.layout.card_mvv),
        NEWS(R.layout.card_news_item),
        NEWS_FILM(R.layout.card_news_film_item),
        EDUROAM(R.layout.card_eduroam),
        CHAT(R.layout.card_chat_messages),
        SUPPORT(R.layout.card_support),
        LOGIN(R.layout.card_login_prompt),
        EDUROAM_FIX(R.layout.card_eduroam_fix),
        TOP_NEWS(R.layout.card_top_news),
        EVENT(R.layout.card_events_item),
        UPDATE_NOTE(R.layout.card_update_note)

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
