package de.tum.`in`.tumcampusapp.utils

import android.location.Location
import de.tum.`in`.tumcampusapp.component.other.locations.Locations
import de.tum.`in`.tumcampusapp.component.other.locations.model.BuildingToGps
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria

object LocationHelper {

    @JvmStatic
    fun calculateDistanceToCafeteria(cafeteria: Cafeteria, location: Location): Float {
        return calculateDistance(
                cafeteria.latitude, cafeteria.longitude, location.latitude, location.longitude
        )
    }

    @JvmStatic
    fun calculateDistanceToCampus(campus: Locations.Campus, location: Location): Float {
        return calculateDistance(campus.lat, campus.lon, location.latitude, location.longitude)
    }

    @JvmStatic
    fun calculateDistanceToBuilding(building: BuildingToGps, location: Location): Float {
        return calculateDistance(
                building.latitude.toDouble(), building.longitude.toDouble(),
                location.latitude, location.longitude
        )
    }

    @JvmStatic
    fun calculateDistance(startLat: Double, startLng: Double,
                          endLat: Double, endLng: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLng, endLat, endLng, results)
        return results[0]
    }

    @JvmStatic
    fun convertUTMtoLL(north: Double, east: Double, zone: Double): Geo {
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

}
