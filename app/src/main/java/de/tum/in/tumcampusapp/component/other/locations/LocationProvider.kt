package de.tum.`in`.tumcampusapp.component.other.locations

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import org.jetbrains.anko.locationManager
import javax.inject.Inject

/**
 * This class provides the last known location of the user in a synchronous way.
 *
 * @param context A [Context]
 */
class LocationProvider @Inject constructor(
        context: Context
) {

    private val locationManager: LocationManager by lazy {
        context.locationManager
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(): Location? {
        // TODO(thellmund) Make async
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

    companion object {

        private var INSTANCE: LocationProvider? = null

        @JvmStatic
        fun getInstance(context: Context): LocationProvider {
            if (INSTANCE == null) {
                INSTANCE = LocationProvider(context)
            }

            return INSTANCE!!
        }

    }

}
