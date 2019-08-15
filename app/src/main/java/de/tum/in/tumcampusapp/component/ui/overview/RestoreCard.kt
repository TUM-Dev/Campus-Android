package de.tum.`in`.tumcampusapp.component.ui.overview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseNavigationActivity
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.component.ui.overview.card.StickyCard

/**
 * Card that allows the user to reset the dismiss state of all cards
 */
class RestoreCard(context: Context) : StickyCard(CardManager.CARD_RESTORE, context) {

    /**
     * Override getPositionByDate, we want the RestoreCard to be the last card.
     */
    override var position: Int
        get() = Integer.MAX_VALUE
        set(value) {
            super.position = value
        }

    override fun getId() = 0

    companion object {

        fun inflateViewHolder(parent: ViewGroup,
                              interactionListener: CardInteractionListener): CardViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_restore, parent, false)
            view.findViewById<View>(R.id.restore_card).setOnClickListener {
                val context = it.context
                if (context is BaseNavigationActivity) {
                    context.restoreCards()
                }
            }
            return CardViewHolder(view, interactionListener)
        }
    }
}
