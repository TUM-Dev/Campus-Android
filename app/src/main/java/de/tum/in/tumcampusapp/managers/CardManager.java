package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tum.in.tumcampusapp.auxiliary.AccessTokenManager;
import de.tum.in.tumcampusapp.cards.EduroamCard;
import de.tum.in.tumcampusapp.cards.EduroamFixCard;
import de.tum.in.tumcampusapp.cards.NoInternetCard;
import de.tum.in.tumcampusapp.cards.RestoreCard;
import de.tum.in.tumcampusapp.cards.Support;
import de.tum.in.tumcampusapp.cards.generic.Card;

/**
 * Card manager, manages inserting, dismissing, updating and displaying of cards
 */
public final class CardManager {
    public static final String SHOW_TUTORIAL_1 = "show_tutorial_1";
    public static final String SHOW_TUTORIAL_2 = "show_tutorial_2";
    public static final String SHOW_SUPPORT = "show_support";

    /**
     * Card typ constants
     */
    public static final int CARD_CAFETERIA = 1;
    public static final int CARD_TUITION_FEE = 2;
    public static final int CARD_NEXT_LECTURE = 3;
    public static final int CARD_RESTORE = 4;
    public static final int CARD_FIRST_USE_1 = 5;
    public static final int CARD_FIRST_USE_2 = 6;
    public static final int CARD_NO_INTERNET = 7;
    public static final int CARD_MVV = 8;
    public static final int CARD_NEWS = 9;
    public static final int CARD_NEWS_FILM = 10;
    public static final int CARD_EDUROAM = 11;
    public static final int CARD_CHAT = 12;
    public static final int CARD_SUPPORT = 13;
    public static final int CARD_SURVEY = 14;
    public static final int CARD_EDUROAM_FIX = 15;
    private static boolean shouldRefresh;
    private static List<Card> cards;
    private static List<Card> newCards;

    /**
     * Adds the specified card to the card manager
     * Should only be used in {@link Card#apply()}
     *
     * @param card Card that should be added
     */
    public static void addCard(Card card) {
        newCards.add(card);
    }

    /**
     * Gets the number of cards
     * HINT: For use in {@link de.tum.in.tumcampusapp.adapters.CardsAdapter}
     *
     * @return Card count
     */
    public static int getCardCount() {
        if (cards == null) {
            return 0;
        }
        return cards.size();
    }

    /**
     * Gets the card by index
     * HINT: For use in {@link de.tum.in.tumcampusapp.adapters.CardsAdapter}
     *
     * @return Card
     */
    public static Card getCard(int pos) {
        return cards.get(pos);
    }

    /**
     * Refreshes or initialises all cards.
     * WARNING: Must not be called from UI thread.
     * <p/>
     * HOW TO ADD A NEW CARD:
     * 1. Let the manager class implement {@link Card.ProvidesCard}
     * 2. Create a new class extending {@link Card}
     * 3. Implement the getCardView method in this class
     * 4. Create a new instance of this card in the
     * {@link Card.ProvidesCard#onRequestCard(Context)} method of the manager
     * 5. Add this card to the CardManager by calling {@link Card#apply()} from
     * {@link Card.ProvidesCard#onRequestCard(Context)}
     * 6. Add an instance of the manager class to the managers list below
     */
    public static synchronized void update(Context context) {
        // Use temporary array to avoid that the main thread is trying to access an empty array
        newCards = new ArrayList<>();

        new NoInternetCard(context).apply();
        new Support(context).apply();

        new EduroamCard(context).apply();
        new EduroamFixCard(context).apply();

        Collection<Card.ProvidesCard> managers = new ArrayList<>();

        // Add those managers only if valid access token is available
        if (new AccessTokenManager(context).hasValidAccessToken()) {
            managers.add(new CalendarManager(context));
            managers.add(new TuitionFeeManager());
            managers.add(new ChatRoomManager(context));
            managers.add(new SurveyManager(context));
        }

        // Those don't need TUMOnline access
        managers.add(new CafeteriaManager(context));
        managers.add(new TransportManager(context));
        managers.add(new NewsManager(context));

        for (Card.ProvidesCard manager : managers) {
            manager.onRequestCard(context);
        }

        // Always append the restore card at the end of our list
        new RestoreCard(context).apply();

        shouldRefresh = false;
        cards = newCards;
    }

    /**
     * Inserts a card into the list
     *
     * @param position Position where the card should be inserted
     * @param item     Card to be inserted
     */
    public static void insert(int position, Card item) {
        if (position < 0 || position > cards.size()) {
            return;
        }
        cards.add(position, item);
    }

    /**
     * Removes card from the list
     *
     * @param position Index of the card to delete
     * @return The removed card object
     */
    public static Card remove(int position) {
        return cards.remove(position);
    }

    /**
     * Removes card from the list
     *
     * @param card The card to delete
     * @return the last index of the card
     */
    public static int remove(Card card) {
        int index = cards.indexOf(card);
        cards.remove(card);
        return index;
    }

    /**
     * Resets dismiss settings for all cards
     */
    public static void restoreCards(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Card.DISCARD_SETTINGS_START, 0);
        prefs.edit()
             .clear()
             .apply();
        AbstractManager.getDb(context)
                       .execSQL("UPDATE news SET dismissed=0");
    }

    public static List<Card> getCards() {
        return cards;
    }

    public static void setShouldRefresh() {
        shouldRefresh = true;
    }

    public static boolean getShouldRefresh() {
        return shouldRefresh;
    }

    private CardManager() {
        // CardManager is a utility class
    }
}
