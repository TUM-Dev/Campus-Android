package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
 * Card that describes how to dismiss a card
 */
public class Support extends Card {

    public Support(Context context) {
        super(context);
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_SUPPORT;
    }

    @Override
    public View getCardView(Context context, ViewGroup parent) {
        super.getCardView(context, parent);

        return mInflater.inflate(R.layout.card_support, parent, false);
    }

    @Override
    public void discard(Editor editor) {
        Utils.setSetting(mContext, CardManager.SHOW_SUPPORT, false);
    }

    @Override
    public boolean shouldShow(SharedPreferences p) {
        return Utils.getSettingBool(mContext, CardManager.SHOW_SUPPORT, true);
    }
}
