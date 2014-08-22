package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.managers.CardManager;


public class RestoreCard extends Card {

    @Override
    public int getTyp() {
        return CardManager.CARD_RESTORE_CARDS;
    }

    @Override
    public View getView(Context context, ViewGroup parent) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return mInflater.inflate(R.layout.card_restore, parent, false);
    }

    @Override
    public void onCardClick(Context context) {
        CardManager.restore();
    }

    @Override
    public void discard(Editor editor) {
    }

    @Override
    public boolean apply(SharedPreferences prefs) {
        CardManager.addCard(this);
        return true;
    }

    @Override
    public boolean isDismissable() {
        return false;
    }
}
