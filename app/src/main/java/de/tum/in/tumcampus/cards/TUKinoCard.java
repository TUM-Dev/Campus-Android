package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.TUKinoActivity;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
 * Card for the TU Kino details page
 */
public class TUKinoCard extends Card {

    public TUKinoCard(Context context){
        super(context, "card_kino");
    }

    @Override
    public int getTyp(){
        return CardManager.CARD_KINO;
    }

    @Override
    public Intent getIntent(){
        Intent i = new Intent(mContext, TUKinoActivity.class);
        return i;
    }

    @Override
    public View getCardView(Context context, ViewGroup parent) {
        super.getCardView(context, parent);
        return mInflater.inflate(R.layout.card_tu_kino, parent, false);
    }
}
