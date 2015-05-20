package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampus.R;
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

    //only start imdb.com at the moment
    //TODO: change to own activity
    @Override
    public Intent getIntent(){
        return new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com"));
    }

    @Override
    public View getCardView(Context context, ViewGroup parent) {
        super.getCardView(context, parent);
        return mInflater.inflate(R.layout.card_tu_kino, parent, false);
    }
}
