package de.tum.`in`.tumcampusapp.component.ui.openinghour

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import java.io.IOException
import javax.inject.Inject

/**
 * Import default location and opening hours
 */
class LocationImportAction @Inject constructor(
        private val database: TcaDb,
        private val tumCabeClient: TUMCabeClient
): DownloadWorker.Action {

    @Throws(IOException::class)
    override fun execute(cacheBehaviour: CacheControl) {
        val openingHours = tumCabeClient.fetchOpeningHours()
        database.locationDao().removeCache()
        database.locationDao().replaceInto(openingHours)
    }

}
