package de.tum.`in`.tumcampusapp.component.ui.overview

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val cardsRepo = CardsRepository(application.applicationContext)

    val cards: LiveData<List<Card>>
        get() = cardsRepo.getCards()

    fun refreshCards() {
        cardsRepo.refreshCards(CacheControl.BYPASS_CACHE)
    }

}