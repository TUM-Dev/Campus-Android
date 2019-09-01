package de.tum.`in`.tumcampusapp.component.ui.overview

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.component.tumui.calendar.NextLectureCard
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeesCard
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaMenuCard
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatMessagesCard
import de.tum.`in`.tumcampusapp.component.ui.eduroam.EduroamCard
import de.tum.`in`.tumcampusapp.component.ui.eduroam.EduroamFixCard
import de.tum.`in`.tumcampusapp.component.ui.news.NewsCard
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsCard
import de.tum.`in`.tumcampusapp.component.ui.onboarding.LoginPromptCard
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventCard
import de.tum.`in`.tumcampusapp.component.ui.transportation.MVVCard
import de.tum.`in`.tumcampusapp.component.ui.updatenote.UpdateNoteCard
import java.util.*

/**
 * Adapter for the cards start page used in [MainActivity]
 */
class CardAdapter(private val interactionListener: CardInteractionListener) : RecyclerView.Adapter<CardViewHolder>() {

    private val mItems = ArrayList<Card>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CardViewHolder {
        when (viewType) {
            CardManager.CARD_CAFETERIA -> return CafeteriaMenuCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_TUITION_FEE -> return TuitionFeesCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_NEXT_LECTURE -> return NextLectureCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_RESTORE -> return RestoreCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_NO_INTERNET -> return NoInternetCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_MVV -> return MVVCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_NEWS, CardManager.CARD_NEWS_FILM -> return NewsCard.inflateViewHolder(viewGroup, viewType, interactionListener)
            CardManager.CARD_EDUROAM -> return EduroamCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_EDUROAM_FIX -> return EduroamFixCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_CHAT -> return ChatMessagesCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_SUPPORT -> return SupportCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_LOGIN -> return LoginPromptCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_TOP_NEWS -> return TopNewsCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_EVENT -> return EventCard.inflateViewHolder(viewGroup, interactionListener)
            CardManager.CARD_UPDATE_NOTE -> return UpdateNoteCard.inflateViewHolder(viewGroup, interactionListener)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun onBindViewHolder(viewHolder: CardViewHolder, position: Int) {
        val card = mItems[position]
        viewHolder.currentCard = card
        card.updateViewHolder(viewHolder)
    }

    override fun getItemViewType(position: Int) = mItems[position].cardType

    override fun getItemId(position: Int): Long {
        val card = mItems[position]
        return (card.cardType + (card.getId() shl 4)).toLong()
    }

    override fun getItemCount() = mItems.size

    internal fun updateItems(newCards: List<Card>) {
        val diffResult = DiffUtil.calculateDiff(Card.DiffCallback(mItems, newCards))

        mItems.clear()
        mItems.addAll(newCards)

        diffResult.dispatchUpdatesTo(this)
    }

    fun remove(position: Int): Card {
        val card = mItems.removeAt(position)
        notifyItemRemoved(position)
        return card
    }

    fun insert(position: Int, card: Card) {
        mItems.add(position, card)
        notifyItemInserted(position)
    }

    internal fun onItemMove(fromPosition: Int, toPosition: Int) {
        val toValidatedPosition = validatePosition(fromPosition, toPosition)
        val card = mItems.removeAt(fromPosition)
        mItems.add(toValidatedPosition, card)

        // Update card positions so they stay the same even when the app is closed
        for (index in mItems.indices) {
            mItems[index].position = index
        }
        notifyItemMoved(fromPosition, toValidatedPosition)
    }

    private fun validatePosition(fromPosition: Int, toPosition: Int): Int {
        val selectedCard = mItems[fromPosition]
        val cardAtPosition = mItems[toPosition]

        // If there is a support card, it should always be the first one
        // except when it's been dismissed.
        // Restore card should stay at the bottom
        if (selectedCard is RestoreCard || selectedCard is SupportCard) {
            return fromPosition
        }

        return when (cardAtPosition) {
            is SupportCard -> toPosition + 1
            is RestoreCard -> toPosition - 1
            else -> toPosition
        }
    }
}
