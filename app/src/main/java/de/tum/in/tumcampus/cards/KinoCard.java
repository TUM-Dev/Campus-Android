package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.KinoActivity;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
 * Card for the TU Kino details page
 */
public class KinoCard extends Card {

    public KinoCard(Context context){
        super(context, "card_kino");
    }

    @Override
    public int getTyp(){
        return CardManager.CARD_KINO;
    }

    @Override
    public Intent getIntent(){
        Intent i = new Intent(mContext, KinoActivity.class);
        return i;
    }

    @Override
    public View getCardView(Context context, ViewGroup parent) {
        super.getCardView(context, parent);
        return mInflater.inflate(R.layout.card_kino, parent, false);
    }
}
