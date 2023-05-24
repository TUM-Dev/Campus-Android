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
        // on get refresh cards
        get() {
            viewModelScope.launch {
                cardsRepo.refreshCards(CacheControl.USE_CACHE)
            }
            return cardsRepo.getCards()
        }

    fun refreshCards() {
        viewModelScope.launch {
            cardsRepo.refreshCards(CacheControl.BYPASS_CACHE)
        }
    }
}