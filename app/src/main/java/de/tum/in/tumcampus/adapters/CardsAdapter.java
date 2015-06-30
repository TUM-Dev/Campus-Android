package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
 * Adapter for the cards start page used in {@link de.tum.in.tumcampus.activities.MainActivity}
 */
public class CardsAdapter extends RecyclerView.Adapter {

    private final Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;

        public ViewHolder(View v, ViewGroup vG) {
            super(v);
            mView = v;
        }
    }


    public CardsAdapter(Context context) {
        mContext = context;
    }


    public Object getItem(int i) {
        return CardManager.getCard(i);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View card = CardManager.getCard(viewType).inflateView(mContext, viewGroup);
        return new ViewHolder(card, viewGroup);
        //@todo implement this in the card types
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        //@todo
    }

    @Override
    public int getItemViewType (int position) {
        return CardManager.getCard(position).getTyp();
    }

    @Override
    public long getItemId(int i) {
        Card card = CardManager.getCard(i);
        return card.getTyp() + card.getId() << 4;
    }

    @Override
    public int getItemCount() {
        return CardManager.getCardCount();
    }

    public Card remove(int position) {
        final Card c = CardManager.remove(position);
        notifyDataSetChanged();
        return c;
    }

    public void insert(int position, Card item) {
        CardManager.insert(position, item);
        this.notifyDataSetChanged();
    }

    public boolean isDismissable(int pos) {
        return CardManager.getCard(pos).isDismissable();
    }
}
