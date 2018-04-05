package de.tum.`in`.tumcampusapp.component.other.locations

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.common.base.Optional
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.other.locations.model.BuildingToGps
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderCoordinate
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.IOException
import java.util.*

/**
 * Location manager, manages intelligent location services, provides methods to easily access
 * the users current location, campus, next public transfer station and best cafeteria
 */
class LocationManager(c: Context) {
    private val mContext: Context = c.applicationContext
    private val buildingToGpsDao: BuildingToGpsDao
    private var manager: android.location.LocationManager? = null

    /**
     * Tests if Google Play services is available and then gets last known position
     *
     * @return Returns the more or less current position or null on failure
     */
    private// If location services are not available use default location if set
    val currentLocation: Location?
        get() {
            if (servicesConnected()) {
                val loc = lastLocation
                if (loc != null) {
                    return loc
                }
            }
            val selectedCampus = Utils.getSetting(mContext, Const.DEFAULT_CAMPUS, "G")
            if ("X" != selectedCampus) {
                val campus = CAMPUS_SHORT[selectedCampus]
                return CAMPUS_LOCATIONS[campus]
            }
            return null
        }

    /**
     * Returns the "id" of the current campus
     *
     * @return Campus id
     */
    private val currentCampus: Campus?
        get() {
            val loc = currentLocation ?: return null
            return getCampusFromLocation(loc)
        }

    /**
     * Returns the cafeteria's identifier which is near the given location
     * The used radius around the cafeteria is 1km.
     *
     * @return Campus id
     */
    private val cafeterias: List<Cafeteria>
        get() {
            val location = currentOrNextLocation

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
    val currentOrNextLocation: Location
        get() {
            return currentLocation ?: nextLocation
        }

    /**
     * Returns the last known location of the device
     *
     * @return The last location
     */

    val lastLocation: Location?
        get() {
            //Check Location permission for Android 6.0
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    } else if (time < minTime &&
                            bestAccuracy == java.lang.Float.MAX_VALUE && time > bestTime) {
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
    val station: StationResult?
        get() {
            val campus = currentCampus ?: return null

            val campusSetting = "card_stations_default_" + campus.short
            val station = Utils.getSetting(mContext, campusSetting, "")
            if ("" != station) {
                ALL_POSSIBLE_DEFAULT_STATIONS.find {
                    it.station == station
                }?.let { return it }
            }
            return DEFAULT_CAMPUS_STATION[campus]
        }

    /**
     * Gets the campus you are currently on or if you are at home or wherever
     * query for your next lecture and find out at which campus it takes place
     */
    private val currentOrNextCampus: Campus?
        get() {
            return currentCampus ?: nextCampus
        }

    /**
     * Provides some intelligence to pick one cafeteria to show
     */
    // If the user is in university or a lecture has been recognized
    // Get nearest cafeteria
    val cafeteria: Int
        get() {
            val campus = currentOrNextCampus
            if (campus != null) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(mContext)
                val defaultVal = DEFAULT_CAMPUS_CAFETERIA[campus]
                val cafeteria = prefs.getString("card_cafeteria_default_" + campus.short, defaultVal)
                if (cafeteria != null) {
                    return Integer.parseInt(cafeteria)
                }
            }
            val list = cafeterias
            return if (list.isEmpty()) {
                -1
            } else list[0].id
        }

    /**
     * Queries your calender and gets the campus at which your next lecture takes place
     */
    private val nextCampus: Campus?
        get() = getCampusFromLocation(nextLocation)

    /**
     * Gets the location of the next room where the user has a lecture.
     * If no lectures are available Garching will be returned
     *
     * @return Location of the next lecture room
     */
    private val nextLocation: Location
        get() {
            val manager = CalendarController(mContext)
            val geo = manager.nextCalendarItemGeo
            val location: Location
            if (geo == null) {
                location = CAMPUS_LOCATIONS[Campus.GarchingForschungszentrum]!!
            } else {
                location = Location("roomfinder")
                location.latitude = java.lang.Double.parseDouble(geo.latitude)
                location.longitude = java.lang.Double.parseDouble(geo.longitude)
            }
            return location
        }

    /**
     * This method tries to get the list of BuildingToGps by querying database or requesting the server.
     * If both two ways fail, it returns Optional.absent().
     *
     * @return The list of BuildingToGps
     */
    private// we have to fetch buildings to gps mapping first.
    val orFetchBuildingsToGps: List<BuildingToGps>
        get() {
            var result: List<BuildingToGps>? = buildingToGpsDao.all
            if (result!!.isEmpty()) {
                try {
                    result = TUMCabeClient.getInstance(mContext).building2Gps
                    if (result == null) {
                        return emptyList()
                    }
                    for (map in result) {
                        buildingToGpsDao.insert(map)
                    }
                } catch (e: IOException) {
                    Utils.log(e)
                    return ArrayList()
                }
            }
            return result
        }

    /**
     * Get Building ID accroding to the current location
     * Do not call on UI thread.
     *
     * @return the id of current building
     */
    val buildingIDFromCurrentLocation: Optional<String>
        get() = getBuildingIDFromLocation(currentLocation!!)

    init {
        val db = TcaDb.getInstance(c)
        buildingToGpsDao = db.buildingToGpsDao()
    }

    /**
     * This might be battery draining
     *
     * @return false if permission check fails
     */
    fun getLocationUpdates(locationListener: LocationListener): Boolean {
        //Check Location permission for Android 6.0
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false
        }

        // Acquire a reference to the system Location Manager
        if (manager == null) {
            manager = mContext.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        }
        // Register the listener with the Location Manager to receive location updates
        manager!!.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1000, 1f, locationListener)
        manager!!.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 1000, 1f, locationListener)
        return true
    }

    fun stopReceivingUpdates(locationListener: LocationListener) {
        if (manager != null) {
            manager!!.removeUpdates(locationListener)
        }
    }

    /**
     * Checks that Google Play services are available
     */
    private fun servicesConnected(): Boolean {
        val resultCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(mContext)
        return if (ConnectionResult.SUCCESS == resultCode) {
            true
        } else {
            Utils.log("Google Play services is NOT available.")
            false
        }
    }

    /**
     * Get the geo information for a room
     *
     * @param archId arch_id of the room
     * @return Location or null on failure
     */
    private fun fetchRoomGeo(archId: String): Optional<Geo> {
        try {
            val coordinate = TUMCabeClient.getInstance(mContext)
                    .fetchCoordinates(archId)
            return convertRoomFinderCoordinateToGeo(coordinate)
        } catch (e: IOException) {
            Utils.log(e)
        }

        return Optional.absent()
    }

    /**
     * Translates room title to Geo
     * HINT: Don't call from UI thread
     *
     * @param roomTitle Room title
     * @return Location or null on failure
     */
    fun roomLocationStringToGeo(roomTitle: String): Optional<Geo> {
        var loc = roomTitle
        if (loc.contains("(")) {
            loc = loc.substring(0, loc.indexOf('('))
                    .trim { it <= ' ' }
        }

        try {
            val rooms = Optional.fromNullable(TUMCabeClient.getInstance(mContext)
                    .fetchRooms(loc))

            if (rooms.isPresent && !rooms.get()
                            .isEmpty()) {
                val room = rooms.get()[0]
                        .arch_id
                return fetchRoomGeo(room)
            }

        } catch (e: IOException) {
            Utils.log(e)
        } catch (e: NullPointerException) {
            Utils.log(e)
        }

        return Optional.absent()
    }

    /**
     * Get Building ID accroding to the given location.
     * Do not call on UI thread.
     *
     * @param location the give location
     * @return the id of current building
     */
    private fun getBuildingIDFromLocation(location: Location): Optional<String> {
        val buildingToGpsList = orFetchBuildingsToGps

        if (buildingToGpsList.isEmpty()) {
            return Optional.absent()
        }

        val lat = location.latitude
        val lng = location.longitude
        val results = FloatArray(1)
        var bestDistance = java.lang.Float.MAX_VALUE
        var bestBuilding = ""

        for ((id, latitude, longitude) in buildingToGpsList) {
            val buildingLat = java.lang.Double.parseDouble(latitude)
            val buildingLng = java.lang.Double.parseDouble(longitude)

            Location.distanceBetween(buildingLat, buildingLng, lat, lng, results)
            val distance = results[0]
            if (distance < bestDistance) {
                bestDistance = distance
                bestBuilding = id
            }
        }

        return if (bestDistance < 1000) {
            Optional.of(bestBuilding)
        } else {
            Optional.absent()
        }
    }

    companion object {
        private enum class Campus(val short: String) {
            GarchingForschungszentrum("G"),
            GarchingHochbrueck("H"),
            Weihenstephan("W"),
            Stammgelaende("C"),
            KlinikumGrosshadern("K"),
            KlinikumRechtsDerIsar("I"),
            Leopoldstrasse("L"),
            GeschwisterSchollplatzAdalbertstrasse("S")
        }

        private val CAMPUS_LOCATIONS = mapOf(
                Campus.GarchingForschungszentrum to Location("defaultLocation").apply { latitude = 48.2648424; longitude = 11.6709511 },
                Campus.GarchingHochbrueck to Location("defaultLocation").apply { latitude = 48.249432; longitude = 11.633905 },
                Campus.Weihenstephan to Location("defaultLocation").apply { latitude = 48.397990; longitude = 11.722727 },
                Campus.Stammgelaende to Location("defaultLocation").apply { latitude = 48.149436; longitude = 11.567635 },
                Campus.KlinikumGrosshadern to Location("defaultLocation").apply { latitude = 48.110847; longitude = 11.4703001 },
                Campus.KlinikumRechtsDerIsar to Location("defaultLocation").apply { latitude = 48.137539; longitude = 11.601119 },
                Campus.Leopoldstrasse to Location("defaultLocation").apply { latitude = 48.155916; longitude = 11.583095 },
                Campus.GeschwisterSchollplatzAdalbertstrasse to Location("defaultLocation").apply { latitude = 48.150244; longitude = 11.580665 }
        )

        private val CAMPUS_SHORT = mapOf(
                "G" to Campus.GarchingForschungszentrum,
                "H" to Campus.GarchingHochbrueck,
                "W" to Campus.Weihenstephan,
                "C" to Campus.Stammgelaende,
                "K" to Campus.KlinikumGrosshadern,
                "I" to Campus.KlinikumRechtsDerIsar,
                "L" to Campus.Leopoldstrasse,
                "S" to Campus.GeschwisterSchollplatzAdalbertstrasse
        )
        private val DEFAULT_CAMPUS_STATION = mapOf(
                Campus.GarchingForschungszentrum to StationResult("Garching-Forschungszentrum", "1000460", Integer.MAX_VALUE),
                Campus.GarchingHochbrueck to StationResult("Garching-Hochbrück", "1000480", Integer.MAX_VALUE),
                Campus.Weihenstephan to StationResult("Weihenstephan", "1002911", Integer.MAX_VALUE),
                Campus.Stammgelaende to StationResult("Theresienstraße", "1000120", Integer.MAX_VALUE),
                Campus.KlinikumGrosshadern to StationResult("Klinikum Großhadern", "1001540", Integer.MAX_VALUE),
                Campus.KlinikumRechtsDerIsar to StationResult("Max-Weber-Platz", "1000580", Integer.MAX_VALUE),
                Campus.Leopoldstrasse to StationResult("Giselastraße", "1000080", Integer.MAX_VALUE),
                Campus.GeschwisterSchollplatzAdalbertstrasse to StationResult("Universität", "1000070", Integer.MAX_VALUE)
        )

        private val ALL_POSSIBLE_DEFAULT_STATIONS = arrayOf(
                StationResult("Garching-Forschungszentrum", "1000460", Integer.MAX_VALUE),
                StationResult("Garching-Hochbrück", "1000480", Integer.MAX_VALUE),
                StationResult("Weihenstephan", "1002911", Integer.MAX_VALUE),
                StationResult("Theresienstraße", "1000120", Integer.MAX_VALUE),
                StationResult("Klinikum Großhadern", "1001540", Integer.MAX_VALUE),
                StationResult("Max-Weber-Platz", "1000580", Integer.MAX_VALUE),
                StationResult("Giselastraße", "1000080", Integer.MAX_VALUE),
                StationResult("Universität", "1000070", Integer.MAX_VALUE),
                StationResult("Pinakotheken", "1000051", Integer.MAX_VALUE),
                StationResult("Technische Universität", "1000095", Integer.MAX_VALUE),
                StationResult("Waldhüterstraße", "1001574", Integer.MAX_VALUE),
                StationResult("LMU Martinsried", "1002557", Integer.MAX_VALUE),
                StationResult("Garching-Technische Universität", "1002070", Integer.MAX_VALUE)
        )

        private val DEFAULT_CAMPUS_CAFETERIA = mapOf(
                Campus.GarchingForschungszentrum to "422",
                Campus.GarchingHochbrueck to null,
                Campus.Weihenstephan to "423",
                Campus.Stammgelaende to "421",
                Campus.KlinikumGrosshadern to "414",
                Campus.KlinikumRechtsDerIsar to null,
                Campus.Leopoldstrasse to "411",
                Campus.GeschwisterSchollplatzAdalbertstrasse to null)

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
            for (l in CAMPUS_LOCATIONS) {
                Location.distanceBetween(l.value.latitude, l.value.longitude, lat, lng, results)
                val distance = results[0]
                if (distance < bestDistance) {
                    bestDistance = distance
                    bestCampus = l.key
                }
            }
            return if (bestDistance < 1000) {
                bestCampus
            } else {
                null
            }
        }

        /**
         * Converts UTM based coordinates to latitude and longitude based format
         */
        private fun convertUTMtoLL(north: Double, east: Double, zone: Double): Geo {
            val d = 0.99960000000000004
            val d1 = 6378137
            val d2 = 0.0066943799999999998
            val d4 = (1 - Math.sqrt(1 - d2)) / (1 + Math.sqrt(1 - d2))
            val d15 = east - 500000
            val d11 = (zone - 1) * 6 - 180 + 3
            val d3 = d2 / (1 - d2)
            val d10 = north / d
            val d12 = d10 / (d1 * (1 - d2 / 4 - (3 * d2 * d2) / 64 - (5 * Math.pow(d2, 3.0)) / 256))
            val d14 = d12 + ((3 * d4) / 2 - (27 * Math.pow(d4, 3.0)) / 32) * Math.sin(2 * d12) + ((21 * d4 * d4) / 16 - (55 * Math.pow(d4, 4.0)) / 32) * Math.sin(4 * d12) + ((151 * Math.pow(d4, 3.0)) / 96) * Math.sin(6 * d12)
            val d5 = d1 / Math.sqrt(1 - d2 * Math.sin(d14) * Math.sin(d14))
            val d6 = Math.tan(d14) * Math.tan(d14)
            val d7 = d3 * Math.cos(d14) * Math.cos(d14)
            val d8 = (d1 * (1 - d2)) / Math.pow(1 - d2 * Math.sin(d14) * Math.sin(d14), 1.5)
            val d9 = d15 / (d5 * d)
            var d17 = d14 - ((d5 * Math.tan(d14)) / d8) * ((d9 * d9) / 2 - ((5 + 3 * d6 + 10 * d7 - 4 * d7 * d7 - 9 * d3) * Math.pow(d9, 4.0)) / 24 + ((61 + 90 * d6 + 298 * d7 + 45 * d6 * d6 - 252 * d3 - 3 * d7 * d7) * Math.pow(d9, 6.0)) / 720)
            d17 *= 180 / Math.PI
            var d18 = (d9 - ((1 + 2 * d6 + d7) * Math.pow(d9, 3.0)) / 6 + ((5 - 2 * d7 + 28 * d6 - 3 * d7 * d7 + 8 * d3 + 24 * d6 * d6) * Math.pow(d9, 5.0)) / 120) / Math.cos(d14)
            d18 = d11 + d18 * 180 / Math.PI
            return Geo(d17, d18)
        }

        fun convertRoomFinderCoordinateToGeo(roomFinderCoordinate: RoomFinderCoordinate): Optional<Geo> {
            val result: Geo
            try {
                val zone = java.lang.Double.parseDouble(roomFinderCoordinate.utm_zone)
                val easting = java.lang.Double.parseDouble(roomFinderCoordinate.utm_easting)
                val northing = java.lang.Double.parseDouble(roomFinderCoordinate.utm_northing)
                result = convertUTMtoLL(northing, easting, zone)

                return Optional.of(result)
            } catch (e: NullPointerException) {
                Utils.log(e)
            } catch (e: NumberFormatException) {
                Utils.log(e)
            }

            return Optional.absent()
        }
    }
}
