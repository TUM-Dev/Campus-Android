package de.tum.in.tumcampus.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import de.tum.in.tumcampus.cards.CafeteriaMenuCard;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.ChatMessagesCard;
import de.tum.in.tumcampus.cards.EduroamCard;
import de.tum.in.tumcampus.cards.FirstUseCard1;
import de.tum.in.tumcampus.cards.FirstUseCard2;
import de.tum.in.tumcampus.cards.MVVCard;
import de.tum.in.tumcampus.cards.NewsCard;
import de.tum.in.tumcampus.cards.NextLectureCard;
import de.tum.in.tumcampus.cards.NoInternetCard;
import de.tum.in.tumcampus.cards.RestoreCard;
import de.tum.in.tumcampus.cards.Support;
import de.tum.in.tumcampus.cards.TuitionFeesCard;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
 * Adapter for the cards start page used in {@link de.tum.in.tumcampus.activities.MainActivity}
 */
public class CardsAdapter extends RecyclerView.Adapter<Card.CardViewHolder> {

    public static Card getItem(int i) {
        return CardManager.getCard(i);
    }

    @Override
    public Card.CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case CardManager.CARD_CAFETERIA:
                return CafeteriaMenuCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_TUITION_FEE:
                return TuitionFeesCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_NEXT_LECTURE:
                return NextLectureCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_RESTORE:
                return RestoreCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_FIRST_USE_1:
                return FirstUseCard1.inflateViewHolder(viewGroup);
            case CardManager.CARD_FIRST_USE_2:
                return FirstUseCard2.inflateViewHolder(viewGroup);
            case CardManager.CARD_NO_INTERNET:
                return NoInternetCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_MVV:
                return MVVCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_NEWS: //Fallthrough
            case CardManager.CARD_NEWS_FILM:
                return NewsCard.inflateViewHolder(viewGroup, viewType);
            case CardManager.CARD_EDUROAM:
                return EduroamCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_CHAT:
                return ChatMessagesCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_SUPPORT:
                return Support.inflateViewHolder(viewGroup);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void onBindViewHolder(Card.CardViewHolder viewHolder, int position) {
        Card card = CardManager.getCard(position);
        viewHolder.setCurrentCard(card);
        card.updateViewHolder(viewHolder);
    }

    @Override
    public int getItemViewType(int position) {
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

    public int remove(Card card) {
        int index = CardManager.remove(card);
        notifyItemRemoved(index);
        return index;
    }

    public Card remove(int position) {
        final Card c = CardManager.remove(position);
        notifyItemRemoved(position);
        return c;
    }

    public void insert(int position, Card item) {
        CardManager.insert(position, item);
        notifyItemInserted(position);
    }
}
