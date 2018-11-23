package de.tum.`in`.tumcampusapp.component.other.locations

import android.location.Location
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult

object Locations {

    enum class Campus(val short: String, val lat: Double, val lon: Double, val defaultMensa: String?, val defaultStation: Stations) {
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

    enum class Stations(val station: StationResult) {
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

}
