package de.tum.in.tumcampusapp.component.ui.overview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.component.tumui.calendar.NextLectureCard;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.TuitionFeesCard;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuCard;
import de.tum.in.tumcampusapp.component.ui.chat.ChatMessagesCard;
import de.tum.in.tumcampusapp.component.ui.eduroam.EduroamCard;
import de.tum.in.tumcampusapp.component.ui.eduroam.EduroamFixCard;
import de.tum.in.tumcampusapp.component.ui.news.NewsCard;
import de.tum.in.tumcampusapp.component.ui.news.TopNewsCard;
import de.tum.in.tumcampusapp.component.ui.onboarding.LoginPromptCard;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.ticket.EventCard;
import de.tum.in.tumcampusapp.component.ui.transportation.MVVCard;

/**
 * Adapter for the cards start page used in {@link MainActivity}
 */
public class CardAdapter extends RecyclerView.Adapter<CardViewHolder> {

    private List<Card> mItems = new ArrayList<>();

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case CardManager.CARD_CAFETERIA:
                return CafeteriaMenuCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_TUITION_FEE:
                return TuitionFeesCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_NEXT_LECTURE:
                return NextLectureCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_RESTORE:
                return RestoreCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_NO_INTERNET:
                return NoInternetCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_MVV:
                return MVVCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_NEWS: //Fallthrough
            case CardManager.CARD_NEWS_FILM:
                return NewsCard.inflateViewHolder(viewGroup, viewType);
            case CardManager.CARD_EDUROAM:
                return EduroamCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_EDUROAM_FIX:
                return EduroamFixCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_CHAT:
                return ChatMessagesCard.Companion.inflateViewHolder(viewGroup);
            case CardManager.CARD_SUPPORT:
                return SupportCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_LOGIN:
                return LoginPromptCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_TOP_NEWS:
                return TopNewsCard.inflateViewHolder(viewGroup);
            case CardManager.CARD_EVENT:
                return EventCard.inflateViewHolder(viewGroup);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder viewHolder, int position) {
        Card card = mItems.get(position);
        viewHolder.setCurrentCard(card);
        card.updateViewHolder(viewHolder);
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getCardType();
    }

    @Override
    public long getItemId(int position) {
        Card card = mItems.get(position);
        return card.getCardType() + (card.getId() << 4);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    void updateItems(List<Card> newCards) {
        DiffUtil.DiffResult diffResult =
                DiffUtil.calculateDiff(new Card.DiffCallback(mItems, newCards));

        mItems.clear();
        mItems.addAll(newCards);

        diffResult.dispatchUpdatesTo(this);
    }

    public Card remove(int position) {
        final Card card = mItems.remove(position);
        notifyItemRemoved(position);
        return card;
    }

    public void insert(int position, Card card) {
        mItems.add(position, card);
        notifyItemInserted(position);
    }

    void onItemMove(int fromPosition, int toPosition) {
        toPosition = validatePosition(fromPosition, toPosition);
        Card card = mItems.remove(fromPosition);
        mItems.add(toPosition, card);

        //Update card positions so they stay the same even when the app is closed
        for (int index = 0; index < mItems.size(); index++) {
            mItems.get(index).setPosition(index);
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    private int validatePosition(int fromPosition, int toPosition) {
        Card selectedCard = mItems.get(fromPosition);
        Card cardAtPosition = mItems.get(toPosition);

        // If there is a support card, it should always be the first one
        // except when it's been dismissed.
        // Restore card should stay at the bottom
        if (selectedCard instanceof RestoreCard) {
            return fromPosition;
        } else if (selectedCard instanceof SupportCard) {
            return fromPosition;
        }

        if (cardAtPosition instanceof SupportCard) {
            return toPosition + 1;
        } else if (cardAtPosition instanceof RestoreCard) {
            return toPosition - 1;
        } else {
            return toPosition;
        }
    }

}
