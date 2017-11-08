package de.tum.`in`.tumcampusapp.models.tumcabe

data class WifiMeasurement(var date: String = "",
                           var ssid: String = "",
                           var bssid: String = "",
                           var dBm: Int = -1,
                           var accuracyInMeters: Float = -1f,
                           var latitude: Double = -1.0,
                           var longitude: Double = -1.0)
