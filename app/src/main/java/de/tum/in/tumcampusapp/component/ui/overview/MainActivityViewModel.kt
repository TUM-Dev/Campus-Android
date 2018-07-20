package de.tum.`in`.tumcampusapp.component.ui.overview

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val cardsRepo = CardsRepository(application.applicationContext)
    private var isFirstRefresh = true

    val cards: LiveData<List<Card>>
        get() = cardsRepo.getCards()

    fun refreshCards() {
        cardsRepo.refreshCards(isFirstRefresh)
        isFirstRefresh.not()
    }

}