package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.SetupEduroam;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.EduroamManager;


public class EduroamCard extends Card {

    public EduroamCard(Context context) {
        super(context);
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_EDUROAM;
    }

    @Override
    public View getCardView(Context context, ViewGroup parent) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return mInflater.inflate(R.layout.card_eduroam, parent, false);
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        EduroamManager manager = new EduroamManager(mContext);
        return !manager.isConfigured();
    }

    @Override
    protected void discard(SharedPreferences.Editor editor) {
        // TODO decide what to do when card is dismissed
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, SetupEduroam.class);
    }
}
