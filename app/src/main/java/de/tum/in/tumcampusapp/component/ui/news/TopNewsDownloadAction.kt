package de.tum.`in`.tumcampusapp.component.ui.news

import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.news.repository.TopNewsRemoteRepository
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import javax.inject.Inject

class TopNewsDownloadAction @Inject constructor(
    private val remoteRepository: TopNewsRemoteRepository
) : DownloadWorker.Action {

    override fun execute(cacheBehaviour: CacheControl) {
        remoteRepository.fetchNewsAlert()
    }
}
