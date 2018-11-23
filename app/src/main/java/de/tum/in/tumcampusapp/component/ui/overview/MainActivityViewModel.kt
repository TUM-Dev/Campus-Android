package de.tum.`in`.tumcampusapp.component.ui.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
        private val cardsRepo: CardsRepository
) : ViewModel() {

    val cards: LiveData<List<Card>>
        get() = cardsRepo.getCards()

    fun refreshCards() {
        cardsRepo.refreshCards(CacheControl.BYPASS_CACHE)
    }

}