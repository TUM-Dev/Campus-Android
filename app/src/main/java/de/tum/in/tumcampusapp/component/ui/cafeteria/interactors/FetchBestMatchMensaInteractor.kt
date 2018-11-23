package de.tum.`in`.tumcampusapp.component.ui.cafeteria.interactors

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager

class FetchBestMatchMensaInteractor(
        private val cafeteriaManager: CafeteriaManager
) : Interactor<Int> {

    override fun execute(): Int {
        return cafeteriaManager.getBestMatchMensaId()
    }

}
