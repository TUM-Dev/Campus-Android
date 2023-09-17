package de.tum.`in`.tumcampusapp.component.other.locations

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.preference.PreferenceManager
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import de.tum.`in`.tumcampusapp.api.navigatum.NavigaTumAPIClient
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import java.lang.Double.parseDouble
import java.util.LinkedList
import javax.inject.Inject

/**
 * Location manager, manages intelligent location services, provides methods to easily access
 * the users current location, campus, next public transfer station and best cafeteria
 */
class LocationManager @Inject constructor(c: Context) {
    private val mContext: Context = c.applicationContext
    private val buildingToGpsDao: BuildingToGpsDao

    init {
        val db = TcaDb.getInstance(c)
        buildingToGpsDao = db.buildingToGpsDao()
    }

    /**
     * Tests if Google Play services is available and then gets last known position
     * If location services are not available use default location if set
     * @return Returns the more or less current position or null on failure
     */
    private fun getCurrentLocation(): Location? {
        if (!servicesConnected()) {
            return null
        }

        val loc = getLastLocation()
        if (loc != null) {
            return loc
        }

        val selectedCampus = Utils.getSetting(mContext, Const.DEFAULT_CAMPUS, "G")
        val allCampi = Campus.values().associateBy(Campus::short)

        if ("X" != selectedCampus && allCampi.containsKey(selectedCampus)) {
            return allCampi[selectedCampus]!!.getLocation()
        }
        return null
    }

    /**
     * Returns the "id" of the current campus
     *
     * @return Campus id
     */
    private fun getCurrentCampus(): Campus? {
        val loc = getCurrentLocation() ?: return null
        return getCampusFromLocation(loc)
    }

    /**
     * Returns the cafeteria's identifier which is near the given location
     * The used radius around the cafeteria is 1km.
     *
     * @return Campus id
     */
    private fun getCafeterias(): List<Cafeteria> {
        val location = getCurrentOrNextLocation()

        val lat = location.latitude
        val lng = location.longitude
        val results = FloatArray(1)
        val list = LinkedList<Cafeteria>()
        for (cafeteria in list) {
            Location.distanceBetween(cafeteria.latitude, cafeteria.longitude, lat, lng, results)
            cafeteria.distance = results[0]
        }
        list.sort()
        return list
    }

    /**
     * Gets the current location and if it is not available guess
     * by querying for the next lecture.
     *
     * @return Any of the above described locations.
     */
    fun getCurrentOrNextLocation(): Location {
        return getCurrentLocation() ?: getNextLocation()
    }

    /**
     * Returns the last known location of the device
     *
     * @return The last location
     */
    private fun getLastLocation(): Location? {
        // Check Location permission for Android 6.0
        if (ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        var bestResult: Location? = null
        var bestAccuracy = java.lang.Float.MAX_VALUE
        var bestTime = java.lang.Long.MIN_VALUE
        val minTime: Long = 0

        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        val matchingProviders = locationManager.allProviders
        for (provider in matchingProviders) {
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null) {
                val accuracy = location.accuracy
                val time = location.time

                if (time > minTime && accuracy < bestAccuracy) {
                    bestResult = location
                    bestAccuracy = accuracy
                    bestTime = time
                } else if (time < minTime && bestAccuracy == java.lang.Float.MAX_VALUE && time > bestTime) {
                    bestResult = location
                    bestTime = time
                }
            }
        }
        return bestResult
    }

    /**
     * Returns the name of the station that is nearby and/or set by the user
     *
     * @return Name of the station or null if the user is not near any campus
     */
    fun getStation(): StationResult? {
        val campus = getCurrentCampus() ?: return null

        // Try to find favorite station for current campus
        val station = Utils.getSetting(mContext, "card_stations_default_" + campus.short, "")
        if (station.isNotEmpty()) {
            Stations.values().associateBy(Stations::station).values.find {
                it.station.station == station
            }?.let { return it.station }
        }
        // Otherwise fallback to the default
        return campus.defaultStation.station
    }

    /**
     * Gets the campus you are currently on or if you are at home or wherever
     * query for your next lecture and find out at which campus it takes place
     */
    private fun getCurrentOrNextCampus(): Campus? {
        return getCurrentCampus() ?: getNextCampus()
    }

    /**
     * If the user is in university or a lecture has been recognized => Get nearest cafeteria
     */
    fun getCafeteria(): Int {
        val campus = getCurrentOrNextCampus()
        if (campus != null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(mContext)
            val cafeteria = prefs.getString("card_cafeteria_default_" + campus.short, campus.defaultMensa)
            if (cafeteria != null) {
                return Integer.parseInt(cafeteria)
            }
        }

        val allCafeterias = getCafeterias()
        return if (allCafeterias.isEmpty()) Const.NO_CAFETERIA_FOUND else allCafeterias[0].id
    }

    /**
     * Queries your calender and gets the campus at which your next lecture takes place
     */
    private fun getNextCampus(): Campus? = getCampusFromLocation(getNextLocation())

    /**
     * Gets the location of the next room where the user has a lecture.
     * If no lectures are available Garching will be returned
     *
     * @return Location of the next lecture room
     */
    private fun getNextLocation(): Location {
        val manager = CalendarController(mContext)
        val geo = manager.nextCalendarItemGeo
        if (geo == null) {
            return Campus.GarchingForschungszentrum.getLocation()
        }

        val location = Location("roomfinder")
        location.latitude = parseDouble(geo.latitude)
        location.longitude = parseDouble(geo.longitude)
        return location
    }

    /**
     * Checks that Google Play services are available
     */
    private fun servicesConnected(): Boolean {
        val resultCode = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS

        Utils.log("Google Play services is $resultCode")
        return resultCode
    }

    /**
     * Translates room title to Geo
     * HINT: Don't call from UI thread
     *
     * @param roomTitle Room title
     * @return Location or null on failure
     */
    fun roomLocationStringToGeo(roomTitle: String): Geo? {
        var loc = roomTitle
        if (loc.contains("(")) {
            loc = loc.substring(0, loc.indexOf('('))
                .trim { it <= ' ' }
        }

        try {
            val searchResult = NavigaTumAPIClient.getInstance(mContext).search(loc)
            var geo: Geo? = null
            if (searchResult != null && searchResult.sections.isNotEmpty() && searchResult.sections[0].entries.isNotEmpty()) {
                val location = searchResult.sections[0].entries[0].id
                // The language does not matter, as we only use the geo information from this request
                val locationDetails = NavigaTumAPIClient.getInstance(mContext).getNavigationDetails(location, "de")
                locationDetails?.let {
                    geo = locationDetails.geo
                }
            }
            return geo
        } catch (e: Exception) {
            Utils.log(e)
        }
        return null
    }

    companion object {
        private enum class Campus(
            val short: String,
            val lat: Double,
            val lon: Double,
            val defaultMensa: String?,
            val defaultStation: Stations
        ) {
            GarchingForschungszentrum("G", 48.2648424, 11.6709511, "422", Stations.GarchingForschungszentrum),
            GarchingHochbrueck("H", 48.249432, 11.633905, null, Stations.GarchingHochbrueck),
            Weihenstephan("W", 48.397990, 11.722727, "423", Stations.Weihenstephan),
            Stammgelaende("C", 48.149436, 11.567635, "421", Stations.Stammgelaende),
            KlinikumGrosshadern("K", 48.110847, 11.4703001, "414", Stations.KlinikumGrosshadern),
            KlinikumRechtsDerIsar("I", 48.137, 11.601119, null, Stations.KlinikumRechtsDerIsar),
            Leopoldstrasse("L", 48.155916, 11.583095, "411", Stations.Leopoldstrasse),
            GeschwisterSchollplatzAdalbertstrasse("S", 48.150244, 11.580665, null, Stations.GeschwisterSchollplatzAdalbertstrasse);

            fun getLocation(): Location {
                return Location("defaultLocation").apply { latitude = lat; longitude = lon }
            }
        }

        private enum class Stations(val station: StationResult) {
            GarchingForschungszentrum(StationResult("Garching-Forschungszentrum", "1000460", Integer.MAX_VALUE)),
            GarchingHochbrueck(StationResult("Garching-Hochbrück", "1000480", Integer.MAX_VALUE)),
            Weihenstephan(StationResult("Weihenstephan", "1002911", Integer.MAX_VALUE)),
            Stammgelaende(StationResult("Theresienstraße", "1000120", Integer.MAX_VALUE)),
            KlinikumGrosshadern(StationResult("Klinikum Großhadern", "1001540", Integer.MAX_VALUE)),
            KlinikumRechtsDerIsar(StationResult("Max-Weber-Platz", "1000580", Integer.MAX_VALUE)),
            Leopoldstrasse(StationResult("Giselastraße", "1000080", Integer.MAX_VALUE)),
            GeschwisterSchollplatzAdalbertstrasse(StationResult("Universität", "1000070", Integer.MAX_VALUE)),
            Pinakotheken(StationResult("Pinakotheken", "1000051", Integer.MAX_VALUE)),
            TUM(StationResult("Technische Universität", "1000095", Integer.MAX_VALUE)),
            Waldhueterstrasse(StationResult("Waldhüterstraße", "1001574", Integer.MAX_VALUE)),
            Martinsried(StationResult("LMU Martinsried", "1002557", Integer.MAX_VALUE)),
            GarchingTUM(StationResult("Garching-Technische Universität", "1002070", Integer.MAX_VALUE))
        }

        /**
         * Returns the "id" of the campus near the given location
         * The used radius around the middle of the campus is 1km.
         *
         * @param location The location to search for a campus
         * @return Campus id
         */
        private fun getCampusFromLocation(location: Location): Campus? {
            val lat = location.latitude
            val lng = location.longitude
            val results = FloatArray(1)
            var bestDistance = java.lang.Float.MAX_VALUE
            var bestCampus: Campus? = null
            for (l in Campus.values()) {
                Location.distanceBetween(l.lat, l.lon, lat, lng, results)
                val distance = results[0]
                if (distance < bestDistance) {
                    bestDistance = distance
                    bestCampus = l
                }
            }

            return if (bestDistance < 1000) {
                bestCampus
            } else {
                null
            }
        }
    }
}
