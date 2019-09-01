package de.tum.`in`.tumcampusapp.component.ui.tufilm

import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import javax.inject.Inject

class FilmDownloadAction @Inject constructor(
    private val kinoRemoteRepository: KinoRemoteRepository
) : DownloadWorker.Action {

    override fun execute(cacheBehaviour: CacheControl) {
        kinoRemoteRepository.fetchKinos(cacheBehaviour == CacheControl.BYPASS_CACHE)
    }
}
