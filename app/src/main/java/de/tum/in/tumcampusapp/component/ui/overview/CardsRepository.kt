package de.tum.`in`.tumcampusapp.component.ui.overview

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crashlytics.android.Crashlytics
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeeManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatRoomController
import de.tum.`in`.tumcampusapp.component.ui.eduroam.EduroamCard
import de.tum.`in`.tumcampusapp.component.ui.eduroam.EduroamFixCard
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsCard
import de.tum.`in`.tumcampusapp.component.ui.onboarding.LoginPromptCard
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.ProvidesCard
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventCardsProvider
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController
import de.tum.`in`.tumcampusapp.component.ui.updatenote.UpdateNoteCard
import de.tum.`in`.tumcampusapp.utils.Utils
import org.jetbrains.anko.doAsync
import javax.inject.Inject

class CardsRepository @Inject constructor(
        private val context: Context,
        private val eventCardsProvider: EventCardsProvider
) {

    private var cards = MutableLiveData<List<Card>>()

    /**
     * Starts refresh of [Card]s and returns the corresponding [LiveData]
     * through which the result can be received.
     *
     * @return The [LiveData] of [Card]s
     */
    fun getCards(): LiveData<List<Card>> {
        refreshCards(CacheControl.USE_CACHE)
        return cards
    }

    /**
     * Refreshes the [LiveData] of [Card]s and updates its value.
     */
    fun refreshCards(cacheControl: CacheControl) {
        doAsync {
            val results = getCardsNow(cacheControl)
            cards.postValue(results)
        }
    }

    /**
     * Returns the list of [Card]s synchronously.
     *
     * @return The list of [Card]s
     */
    private fun getCardsNow(cacheControl: CacheControl): List<Card> {
        val results = ArrayList<Card?>().apply {
            add(NoInternetCard(context).getIfShowOnStart())
            add(TopNewsCard(context).getIfShowOnStart())
            add(LoginPromptCard(context).getIfShowOnStart())
            add(SupportCard(context).getIfShowOnStart())
            add(EduroamCard(context).getIfShowOnStart())
            add(EduroamFixCard(context).getIfShowOnStart())
            add(UpdateNoteCard(context).getIfShowOnStart())
        }

        val providers = ArrayList<ProvidesCard>().apply {
            if (AccessTokenManager.hasValidAccessToken(context)) {
                add(CalendarController(context))
                add(TuitionFeeManager(context))
                add(ChatRoomController(context))
            }
            add(CafeteriaManager(context))
            add(TransportController(context))
            add(NewsController(context))
            add(eventCardsProvider)
        }

        providers.forEach { provider ->
            // Don't prevent a single card exception to block other cards from being displayed.
            try {
                val cards = provider.getCards(cacheControl)
                results.addAll(cards)
            } catch (e: Exception) {
                // We still want to know about it though
                Utils.log(e)
                Crashlytics.logException(e)
            }
        }

        results.add(RestoreCard(context).getIfShowOnStart())

        return results.filterNotNull()
    }

}