package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardsProvider
import de.tum.`in`.tumcampusapp.database.TcaDb
import java.util.*
import javax.inject.Inject

class CalendarCardsProvider @Inject constructor(
        private val context: Context,
        private val database: TcaDb
) : CardsProvider {

    override fun provideCards(cacheControl: CacheControl): List<Card> {
        val nextCalendarItems = database.calendarDao().nextUniqueCalendarItems
        val results = ArrayList<Card>()

        if (!nextCalendarItems.isEmpty()) {
            val card = NextLectureCard(context)
            card.setLectures(nextCalendarItems)

            card.getIfShowOnStart()?.let {
                results.add(it)
            }
        }

        return results
    }

}
