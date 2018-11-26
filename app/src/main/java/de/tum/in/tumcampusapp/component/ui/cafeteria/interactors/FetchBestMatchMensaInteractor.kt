package de.tum.`in`.tumcampusapp.component.ui.cafeteria.interactors

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import javax.inject.Inject

class FetchBestMatchMensaInteractor @Inject constructor(
        private val cafeteriaManager: CafeteriaManager
) : Interactor<Int> {

    override fun execute(): Int {
        return cafeteriaManager.getBestMatchMensaId()
    }

}
