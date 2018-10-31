package de.tum.`in`.tumcampusapp.component.ui.overview.card

import androidx.annotation.NonNull
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl

/**
 * Interface which has to be implemented by a manager class to add cards to the stream
 */
interface ProvidesCard {

    /**
     * Returns the list of [Card]s that should be displayed in the overview screen.
     */
    fun getCards(@NonNull cacheControl: CacheControl): List<Card>

}