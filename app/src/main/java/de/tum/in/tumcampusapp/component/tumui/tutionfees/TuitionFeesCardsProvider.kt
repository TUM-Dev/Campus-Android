package de.tum.`in`.tumcampusapp.component.tumui.tutionfees

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardsProvider
import java.util.*
import javax.inject.Inject

class TuitionFeesCardsProvider @Inject constructor(
        private val context: Context,
        private val tuitionFeeManager: TuitionFeeManager
) : CardsProvider {

    override fun provideCards(cacheControl: CacheControl): List<Card> {
        val results = ArrayList<Card>()
        val tuition = tuitionFeeManager.loadTuition(cacheControl) ?: return results


        val card = TuitionFeesCard(context)
        card.setTuition(tuition)

        card.getIfShowOnStart()?.let {
            results.add(it)
        }
        return results
    }

}
