package de.tum.in.tumcampusapp.component.ui.overview;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Utils;

import static de.tum.in.tumcampusapp.utils.Const.CARD_POSITION_PREFERENCE_SUFFIX;
import static de.tum.in.tumcampusapp.utils.Const.DISCARD_SETTINGS_START;

/**
 * Card manager, manages inserting, dismissing, updating and displaying of cards
 */
public final class CardManager {

    public static final String SHOW_SUPPORT = "show_support";
    public static final String SHOW_LOGIN = "show_login";
    public static final String SHOW_TOP_NEWS = "show_top_news";

    /**
     * Card typ constants
     */
    public static final int CARD_CAFETERIA = R.layout.card_cafeteria_menu;
    public static final int CARD_TUITION_FEE = R.layout.card_tuition_fees;
    public static final int CARD_NEXT_LECTURE = R.layout.card_next_lecture_item;
    public static final int CARD_RESTORE = R.layout.card_restore;
    public static final int CARD_NO_INTERNET = R.layout.card_no_internet;
    public static final int CARD_MVV = R.layout.card_mvv;
    public static final int CARD_NEWS = R.layout.card_news_item;
    public static final int CARD_NEWS_FILM = R.layout.card_news_film_item;
    public static final int CARD_EDUROAM = R.layout.card_eduroam;
    public static final int CARD_CHAT = R.layout.card_chat_messages;
    public static final int CARD_SUPPORT = R.layout.card_support;
    public static final int CARD_LOGIN = R.layout.card_login_prompt;
    public static final int CARD_EDUROAM_FIX = R.layout.card_eduroam_fix;
    public static final int CARD_TOP_NEWS = R.layout.card_top_news;
    public static final int CARD_EVENT = R.layout.card_events_item;
    public static final int CARD_UPDATE_NOTE = R.layout.card_update_note;

    private CardManager() {}

    /**
     * Resets dismiss settings for all cards
     */
    public static void restoreCards(Context context) {
        context.getSharedPreferences(DISCARD_SETTINGS_START, 0)
               .edit()
               .clear()
               .apply();

        TcaDb.Companion.getInstance(context)
                       .newsDao()
                       .restoreAllNews();

        Utils.setSetting(context, SHOW_TOP_NEWS, true);
        restoreCardPositions(context);
    }

    private static void restoreCardPositions(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = preferences.edit();

        for (String s : preferences.getAll().keySet()) {
            if (s.endsWith(CARD_POSITION_PREFERENCE_SUFFIX)) {
                editor.remove(s);
            }
        }
        editor.apply();
    }

}
