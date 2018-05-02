package de.tum.`in`.tumcampusapp.component.other.wifimeasurement.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.net.wifi.ScanResult
import de.tum.`in`.tumcampusapp.utils.DateUtils
import java.util.*

@Entity(tableName = "wifi_measurement")
data class WifiMeasurement(@PrimaryKey
                           var date: String = DateUtils.getDateTimeString(Date()),
                           var ssid: String = "",
                           var bssid: String = "",
                           var dBm: Int = -1,
                           var accuracyInMeters: Float = -1f,
                           var latitude: Double = -1.0,
                           var longitude: Double = -1.0) {
    companion object {

        fun create(ssid: String, bssid: String, dBm: Int, accuracyInMeters: Float, latitude: Double, longitude: Double): WifiMeasurement {
            return WifiMeasurement(ssid = ssid, bssid = bssid, dBm = dBm,
                    accuracyInMeters = accuracyInMeters, latitude = latitude, longitude = longitude)
        }

        fun fromScanResult(scanResult: ScanResult): WifiMeasurement {
            return WifiMeasurement("", scanResult.SSID, scanResult.BSSID, scanResult.level, -1f, -1.0, -1.0)
        }

    }
}
