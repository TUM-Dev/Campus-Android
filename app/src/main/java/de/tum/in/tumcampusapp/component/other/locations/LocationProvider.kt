package de.tum.`in`.tumcampusapp.component.other.locations

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import org.jetbrains.anko.locationManager
import javax.inject.Inject

class LocationProvider @Inject constructor(
        context: Context
) {

    private val locationProvider = LocationServices.getFusedLocationProviderClient(context)

    private val locationManager: LocationManager by lazy {
        context.locationManager
    }

    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun getLastLocation(): Location? {
        // TODO: Make async
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
