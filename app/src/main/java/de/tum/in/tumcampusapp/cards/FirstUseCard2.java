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
 * Card that describes how to change card settings
 */
public class FirstUseCard2 extends Card {

    public FirstUseCard2(Context context) {
        super(CardManager.CARD_FIRST_USE_2, context);
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_first_use2, parent, false);
        return new Card.CardViewHolder(view);
    }

    @Override
    public void discard(Editor editor) {
        Utils.setInternalSetting(mContext, CardManager.SHOW_TUTORIAL_2, false);
    }

    @Override
    protected boolean shouldShow(SharedPreferences p) {
        return Utils.getInternalSettingBool(mContext, CardManager.SHOW_TUTORIAL_2, true);
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
