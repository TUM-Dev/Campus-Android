package de.tum.in.tumcampusapp.component.ui.overview;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

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
    public static final int CARD_CAFETERIA = 1;
    public static final int CARD_TUITION_FEE = 2;
    public static final int CARD_NEXT_LECTURE = 3;
    public static final int CARD_RESTORE = 4;
    public static final int CARD_NO_INTERNET = 7;
    public static final int CARD_MVV = 8;
    public static final int CARD_NEWS = 9;
    public static final int CARD_NEWS_FILM = 10;
    public static final int CARD_EDUROAM = 11;
    public static final int CARD_CHAT = 12;
    public static final int CARD_SUPPORT = 13;
    public static final int CARD_LOGIN = 14;
    public static final int CARD_EDUROAM_FIX = 15;
    public static final int CARD_TOP_NEWS = 16;

    private CardManager() {}

    /**
     * Resets dismiss settingsPrefix for all cards
     */
    public static void restoreCards(Context context) {
        context.getSharedPreferences(DISCARD_SETTINGS_START, 0)
               .edit()
               .clear()
               .apply();

        TcaDb.getInstance(context)
             .newsDao()
             .restoreAllNews();

        Utils.setSetting(context, SHOW_TOP_NEWS, true);
        restoreCardPositions(context);
    }

    private static void restoreCardPositions(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = preferences.edit();
        for (String s : preferences.getAll()
                                   .keySet()) {
            if (s.endsWith(CARD_POSITION_PREFERENCE_SUFFIX)) {
                editor.remove(s);
            }
        }
        editor.apply();
    }

}
