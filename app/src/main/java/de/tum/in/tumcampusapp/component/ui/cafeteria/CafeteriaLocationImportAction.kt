package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Location
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.tcaDb
import java.io.IOException

/**
 * Import default location and opening hours from assets
 */
class CafeteriaLocationImportAction(private val context: Context): (CacheControl) -> Unit {

    @Throws(IOException::class)
    override fun invoke(cacheBehaviour: CacheControl) {
        val dao = context.tcaDb.locationDao()
        if (dao.isEmpty) {
            Utils.readCsv(context.assets.open(CSV_LOCATIONS))
                    .map(Location.Companion::fromCSVRow)
                    .forEach(dao::replaceInto)
        }
    }

    companion object {
        private const val CSV_LOCATIONS = "locations.csv"
    }
}
