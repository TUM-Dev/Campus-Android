package de.tum.in.tumcampusapp.component.ui.overview;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.overview.card.StickyCard;

/**
 * Card that allows the user to reset the dismiss state of all cards
 */
public class RestoreCard extends StickyCard {

    public RestoreCard(Context context) {
        super(CardManager.CARD_RESTORE, context);
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_restore, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public Intent getIntent() {
        return null;
    }

    @Override
    public int getId() {
        return 0;
    }

    /**
     * Override getPosition, we want the RestoreCard to be the last card.
     */
    @Override
    public int getPosition() {
        return Integer.MAX_VALUE;
    }
}
