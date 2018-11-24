package de.tum.`in`.tumcampusapp.component.other.locations

import android.content.Context
import android.location.Location
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.other.locations.model.BuildingToGps
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderCoordinate
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.LocationHelper
import de.tum.`in`.tumcampusapp.utils.LocationHelper.calculateDistanceToBuilding
import de.tum.`in`.tumcampusapp.utils.LocationHelper.calculateDistanceToCampus
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.tryOrNull
import java.io.IOException
import java.lang.Double.parseDouble

/**
 * Location manager, manages intelligent location services, provides methods to easily access
 * the users current location, campus, next public transfer station and best cafeteria
 */
class TumLocationManager(context: Context) {

    private val context = context.applicationContext
    private val buildingToGpsDao: BuildingToGpsDao by lazy {
        TcaDb.getInstance(context).buildingToGpsDao()
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

        val loc = LocationProvider.getInstance(context).getLastLocation()
        loc?.let {
            return it
        }

        val selectedCampus = Utils.getSetting(context, Const.DEFAULT_CAMPUS, "G")
        val allCampuses = Locations.Campus.values().associateBy(Locations.Campus::short)

        if ("X" != selectedCampus && allCampuses.containsKey(selectedCampus)) {
            return allCampuses.getValue(selectedCampus).getLocation()
        }
        return null
    }

    /**
     * Returns the "id" of the current campus
     *
     * @return Locations.Campus id
     */
    private fun getCurrentCampus(): Locations.Campus? {
        val loc = getCurrentLocation() ?: return null
        return getCampusFromLocation(loc)
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
     * Returns the name of the station that is nearby and/or set by the user
     *
     * @return Name of the station or null if the user is not near any campus
     */
    fun getStation(): StationResult? {
        val campus = getCurrentCampus() ?: return null

        // Try to find favorite station for current campus
        val favoriteStation = Utils.getSetting(context, "card_stations_default_" + campus.short, "")
        if (favoriteStation.isNotEmpty()) {
            val stations = Locations.Stations.values().associateBy(Locations.Stations::station).values
            val candidate = stations.find { it.station.station == favoriteStation }
            candidate?.let {
                return it.station
            }
        }

        // Otherwise fallback to the default
        return campus.defaultStation.station
    }

    /**
     * Gets the campus you are currently on or if you are at home or wherever
     * query for your next lecture and find out at which campus it takes place
     */
    fun getCurrentOrNextCampus(): Locations.Campus? {
        return getCurrentCampus() ?: getNextCampus()
    }

    /**
     * Queries your calender and gets the campus at which your next lecture takes place
     */
    private fun getNextCampus(): Locations.Campus? = getCampusFromLocation(getNextLocation())

    /**
     * Gets the location of the next room where the user has a lecture.
     * If no lectures are available Garching will be returned
     *
     * @return Location of the next lecture room
     */
    private fun getNextLocation(): Location {
        val manager = CalendarController(context)
        val geo = manager.nextCalendarItemGeo ?: return Locations.Campus.GarchingForschungszentrum.getLocation()

        val location = Location("roomfinder")
        location.latitude = parseDouble(geo.latitude)
        location.longitude = parseDouble(geo.longitude)
        return location
    }

    private fun loadBuildingToGpsList(): List<BuildingToGps> {
        val results = buildingToGpsDao.getAll()
        if (results.isNotEmpty()) {
            return results
        }

        val apiResults = tryOrNull { TUMCabeClient.getInstance(context).building2Gps } ?: emptyList()
        apiResults.forEach {
            buildingToGpsDao.insert(it)
        }

        return apiResults
    }

    /**
     * Get Building ID accroding to the current location
     * Do not call on UI thread.
     *
     * @return the id of current building
     */
    fun getBuildingIDFromCurrentLocation(): String? = getBuildingIDFromLocation(getCurrentOrNextLocation())

    /**
     * Checks that Google Play services are available
     */
    private fun servicesConnected(): Boolean {
        val resultCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

        Utils.log("Google Play services is $resultCode")
        return resultCode
    }

    /**
     * Get the geo information for a room
     *
     * @param archId arch_id of the room
     * @return Location or null on failure
     */
    private fun fetchRoomGeo(archId: String): Geo? {
        return try {
            val coordinate = TUMCabeClient.getInstance(context).fetchCoordinates(archId)
            convertRoomFinderCoordinateToGeo(coordinate)
        } catch (e: IOException) {
            Utils.log(e)
            null
        }
    }

    /**
     * Translates room title to Geo
     * HINT: Don't call from UI thread
     *
     * @param roomTitle Room title
     * @return Location or null on failure
     */
    fun roomLocationStringToGeo(roomTitle: String): Geo? {
        val location = if (roomTitle.contains("(")) {
            roomTitle.substring(0, roomTitle.indexOf('(')).trim { it <= ' ' }
        } else {
            roomTitle
        }

        val rooms = tryOrNull { TUMCabeClient.getInstance(context).fetchRooms(location) }
        return rooms?.firstOrNull()?.let {
            fetchRoomGeo(it.arch_id)
        }
    }

    /**
     * Get Building ID accroding to the given location.
     * Do not call on UI thread.
     *
     * @param location the give location
     * @return the id of current building
     */
    private fun getBuildingIDFromLocation(location: Location): String? {
        val buildingToGpsList = loadBuildingToGpsList()
        if (buildingToGpsList.isEmpty()) {
            return null
        }

        return buildingToGpsList
                .map {
                    val distance = calculateDistanceToBuilding(it, location)
                    Pair(it, distance)
                }
                .filter { it.second < 1_000 }
                .sortedBy { it.second }
                .map { it.first.id }
                .firstOrNull()
    }

    companion object {

        /**
         * Returns the "id" of the campus near the given location
         * The used radius around the middle of the campus is 1km.
         *
         * @param location The location to search for a campus
         * @return Campus id
         */
        private fun getCampusFromLocation(location: Location): Locations.Campus? {
            val campuses = Locations.Campus.values()
            return campuses
                    .map {
                        val distance = calculateDistanceToCampus(it, location)
                        Pair(it, distance)
                    }
                    .filter { it.second < 1_000 }
                    .sortedBy { it.second }
                    .map { it.first }
                    .firstOrNull()
        }

        @JvmStatic
        fun convertRoomFinderCoordinateToGeo(roomFinderCoordinate: RoomFinderCoordinate): Geo? {
            return try {
                val zone = parseDouble(roomFinderCoordinate.utm_zone)
                val easting = parseDouble(roomFinderCoordinate.utm_easting)
                val northing = parseDouble(roomFinderCoordinate.utm_northing)
                LocationHelper.convertUTMtoLL(northing, easting, zone)
            } catch (e: Exception) {
                Utils.log(e)
                null
            }
        }
    }
}
