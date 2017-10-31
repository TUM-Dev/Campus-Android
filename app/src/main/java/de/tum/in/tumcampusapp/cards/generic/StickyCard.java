package de.tum.in.tumcampusapp.cards.generic;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class StickyCard extends Card {

    public StickyCard(int cardType, Context context) {
        super(cardType, context);
    }

    @Override
    public final boolean isDismissible() {
        return false;
    }

    @Override
    protected final void discard(SharedPreferences.Editor editor) {
        // Sticky cards can't be dismissed
    }
}
