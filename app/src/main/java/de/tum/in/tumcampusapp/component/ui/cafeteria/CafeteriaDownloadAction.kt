package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import javax.inject.Inject

class CafeteriaDownloadAction @Inject constructor(
    private val cafeteriaRemoteRepository: CafeteriaRemoteRepository
) : DownloadWorker.Action {

    override fun execute(cacheBehaviour: CacheControl) {
        cafeteriaRemoteRepository.fetchCafeterias(cacheBehaviour == CacheControl.BYPASS_CACHE)
    }
}
