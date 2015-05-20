package de.tum.in.tumcampus.models.managers;

import android.content.Context;

import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.TUKinoCard;

/**
 * TU Kino Manager, handles content and card creation
 */
public class TUKinoManager implements Card.ProvidesCard {

    /**
     * Add TU Kino Card to the stream
     * @param context Context
     */
    @Override
    public void onRequestCard(Context context){
        Card card = new TUKinoCard(context);
        card.apply();
    }
}
