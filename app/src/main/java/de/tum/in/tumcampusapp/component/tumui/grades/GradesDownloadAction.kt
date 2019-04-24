package de.tum.`in`.tumcampusapp.component.tumui.grades

import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import javax.inject.Inject

class GradesDownloadAction @Inject constructor(
        private val updater: GradesBackgroundUpdater
) : DownloadWorker.Action {

    override fun execute(cacheBehaviour: CacheControl) {
        updater.fetchGradesAndNotifyIfNecessary()
    }

}
