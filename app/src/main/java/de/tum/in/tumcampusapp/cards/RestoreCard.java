package de.tum.in.tumcampusapp.cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.models.managers.CardManager;

/**
 * Card that allows the user to reset the dismiss state of all cards
 */
public class RestoreCard extends Card {

    public RestoreCard(Context context) {
        super(context);
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_restore, parent, false);
        return new Card.CardViewHolder(view);
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_RESTORE;
    }

    @Override
    public boolean isDismissable() {
        return false;
    }
}
