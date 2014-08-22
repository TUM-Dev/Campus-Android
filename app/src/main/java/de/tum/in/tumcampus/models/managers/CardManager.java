package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.RestoreCard;

public class CardManager {
    public static final int CARD_CAFETERIA = 1;
    public static final int CARD_TUITION_FEE = 2;
    public static final int CARD_NEXT_LECTURE = 3;
    public static final int CARD_RESTORE_CARDS = 4;

    private static List<Card> cards;
    private static List<ProvidesCard> managers;
    private static ArrayList<Card> newCards;
    private static Context mContext;
    private static boolean mRefresh = false;

    public static void addCard(Card card) {
        newCards.add(card);
    }

    public static int getCardCount() {
        return cards.size();
    }

    public static Card getCard(int pos) {
        return cards.get(pos);
    }


    /** HOWTO ADD A NEW CARD TYP
     * 1. let the manager class implement ProvidesCard
     * 2. Create a new class extending Card
     * 3. implement the getView method in this class
     * 4. create a new instance of this card in the onRequestCard of the manager
     * 5. add this card to the CardManager by calling addCard(card)
     * 6. add an instance of the manager class to the managers list below
     * */
    public static void update(Context context) {
        mContext = context;

        // Use temporary array to avoid that the main thread is
        // trying to access an empty array
        newCards = new ArrayList<Card>();
        managers = new ArrayList<ProvidesCard>();

        // Add those managers only if valid access token is available
        if(new AccessTokenManager(context).hasValidAccessToken()) {
            managers.add(new CalendarManager(context));
            managers.add(new TuitionFeeManager());
        }
        // Those don't need TUMOnline access
        managers.add(new CafeteriaManager(context));

        for(ProvidesCard manager : managers){
            try{
                manager.onRequestCard(context);
            }catch(Exception ex){
                Log.e("TCA", "Error while creating card", ex);
            }
        }

        // Always append the restore card at the end of our list
        new RestoreCard().apply();

        cards = newCards;
    }

    public static boolean onCardClicked(Context context, int position) {
        cards.get(position).onCardClick(context);
        if(mRefresh) {
            mRefresh = false;
            return true;
        }
        return false;
    }

    public static Card remove(int position) {
        return cards.remove(position);
    }

    public static void insert(int position, Card item) {
        cards.add(position, item);
    }

    public static Context getContext() {
        return mContext;
    }

    public static void restore() {
        SharedPreferences prefs = CardManager.getContext().getSharedPreferences(Card.DISCARD_SETTINGS, 0);
        prefs.edit().clear().commit();
        update(mContext);
        mRefresh = true;
    }
}
