package de.tum.`in`.tumcampusapp.component.ui.overview.card

import android.content.Context

/**
 * Interface which has to be implemented by a manager class to add cards to the stream
 */
interface ProvidesCard {
    /**
     * Gets called whenever cards need to be shown or refreshed.
     * This method should decide whether a card can be displayed and if so
     * call [Card.apply] to tell the card manager.
     *
     * @param context Context
     */
    fun onRequestCard(context: Context)
}