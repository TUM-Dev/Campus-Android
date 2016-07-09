package de.tum.in.tumcampusapp.cards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.models.managers.CardManager;

/**
 * Card that describes how to dismiss a card
 */
public class FirstUseCard1 extends Card {

    public FirstUseCard1(Context context) {
        super(CardManager.CARD_FIRST_USE_1, context);
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_first_use1, parent, false);
        return new Card.CardViewHolder(view);
    }

    @Override
    public void discard(Editor editor) {
        Utils.setInternalSetting(mContext, CardManager.SHOW_TUTORIAL_1, false);
    }

    @Override
    protected boolean shouldShow(SharedPreferences p) {
        return Utils.getInternalSettingBool(mContext, CardManager.SHOW_TUTORIAL_1, true);
    }

    @Override
    public Intent getIntent() {
        return null;
    }

    @Override
    public int getId() {
        return 0;
    }
}
