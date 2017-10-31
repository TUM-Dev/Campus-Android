package de.tum.`in`.tumcampusapp.models.tumcabe

data class WifiMeasurement(val date: String, val ssid: String, val bssid: String, val dBm: Int, var accuracyInMeters: Float,
                           var latitude: Double, var longitude: Double)
