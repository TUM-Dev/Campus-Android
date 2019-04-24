package de.tum.`in`.tumcampusapp.component.ui.news

import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import javax.inject.Inject

class NewsDownloadAction @Inject constructor(
        private val newsController: NewsController
) : DownloadWorker.Action {

    override fun execute(cacheBehaviour: CacheControl) {
        newsController.downloadFromExternal(cacheBehaviour)
    }

}
