package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import de.tum.in.tumcampus.auxiliary.SwipeDismissList;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
 * Adapter for the cards start page used in {@link de.tum.in.tumcampus.activities.MainActivity}
 */
public class CardsAdapter extends BaseAdapter implements SwipeDismissList.SwipeDismissDiscardable {

    private final Context mContext;

    public CardsAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return CardManager.getCardCount();
    }

    @Override
    public Object getItem(int i) {
        return CardManager.getCard(i);
    }

    @Override
    public long getItemId(int i) {
        Card card = CardManager.getCard(i);
        return card.getTyp()+card.getId()<<4;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        return CardManager.getCard(position).getCardView(mContext, viewGroup);
    }

    public Card remove(int position) {
        final Card c = CardManager.remove(position);
        notifyDataSetChanged();
        return c;
    }

    public void insert(int position, Card item) {
        CardManager.insert(position, item);
        notifyDataSetChanged();
    }

    @Override
    public boolean isDismissable(int pos) {
        return CardManager.getCard(pos).isDismissable();
    }
}
