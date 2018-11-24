package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardsProvider
import de.tum.`in`.tumcampusapp.database.TcaDb
import java.util.*
import javax.inject.Inject

class EventCardsProvider @Inject constructor(
        private val context: Context,
        private val database: TcaDb
) : CardsProvider {

    override fun provideCards(cacheControl: CacheControl): List<Card> {
        val results = ArrayList<Card>()

        // Add the next upcoming event that is not the next kino event
        val event = database.eventDao().nextEventWithoutMovie
        if (event != null) {
            val eventCard = EventCard(context)
            eventCard.event = event
            results.add(eventCard)
        }

        return results
    }

}
