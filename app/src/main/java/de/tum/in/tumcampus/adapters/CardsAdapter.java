package de.tum.in.tumcampus.adapters;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.SwipeDismissList;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.models.managers.CardManager;

public class CardsAdapter extends BaseAdapter implements SwipeDismissList.SwipeDismissDiscardable {

    private Context mContext;
    private int lastPosition = -1;

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
        return card.getTyp();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = CardManager.getCard(position).getCardView(mContext, viewGroup);

        if(position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.up_from_bottom);
            view.startAnimation(animation);
        }
        lastPosition = position;

        return view;
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
