package de.tum.`in`.tumcampusapp.component.other.wifimeasurement.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import android.net.wifi.ScanResult
import org.joda.time.DateTime

@Entity(tableName = "wifi_measurement")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class WifiMeasurement(@PrimaryKey
                           var date: DateTime = DateTime(),
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
            return WifiMeasurement(ssid = scanResult.SSID, bssid = scanResult.BSSID, dBm = scanResult.level,
                    accuracyInMeters = -1f, latitude = -1.0, longitude = -1.0)
        }

    }
}
