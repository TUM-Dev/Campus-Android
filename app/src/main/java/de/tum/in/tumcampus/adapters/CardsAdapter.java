package de.tum.in.tumcampus.adapters;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.models.managers.CardManager;

public class CardsAdapter extends BaseAdapter {

    private Context mContext;

    public CardsAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return CardManager.getCardCount();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        Card card = CardManager.getCard(i);
        return card.getTyp();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return CardManager.getCard(i).getView(mContext,viewGroup);
    }
}
