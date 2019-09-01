package de.tum.`in`.tumcampusapp.utils

import android.location.Location
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria

object LocationHelper {

    fun calculateDistanceToCafeteria(cafeteria: Cafeteria, location: Location): Float {
        val results = FloatArray(1)
        Location.distanceBetween(cafeteria.latitude, cafeteria.longitude,
                location.latitude, location.longitude, results)
        return results[0]
    }
}
