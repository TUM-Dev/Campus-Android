package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.content.res.AssetManager
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Location
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.IOException
import javax.inject.Inject

/**
 * Import default location and opening hours from assets
 */
class CafeteriaLocationImportAction @Inject constructor(
        private val assetManager: AssetManager,
        private val database: TcaDb
): DownloadWorker.Action {

    @Throws(IOException::class)
    override fun execute(cacheBehaviour: CacheControl) {
        val dao = database.locationDao()
        if (dao.isEmpty) {
            Utils.readCsv(assetManager.open(CSV_LOCATIONS))
                    .map(Location.Companion::fromCSVRow)
                    .forEach(dao::replaceInto)
        }
    }

    companion object {
        private const val CSV_LOCATIONS = "locations.csv"
    }

}
