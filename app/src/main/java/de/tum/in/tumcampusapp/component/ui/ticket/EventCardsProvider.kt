package de.tum.`in`.tumcampusapp.component.ui.ticket

import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.ProvidesCard
import java.util.*
import javax.inject.Inject

class EventCardsProvider @Inject
constructor() : ProvidesCard {

    fun setDismissed(id: Int) {
    }

    override fun getCards(cacheControl: CacheControl): List<Card> {
        return ArrayList<Card>()
    }
}
