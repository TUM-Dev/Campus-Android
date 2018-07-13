package de.tum.`in`.tumcampusapp.component.other.general

import java.io.Serializable

/**
 * Used for parsing the FCM json payload for the 'update' type
 */
data class FcmUpdate(var packageVersion: Int = 0,
                     var sdkVersion: Int = 0,
                     var releaseDate: String = "",
                     var lowestVersion: Int = 0) : Serializable {
    companion object {
        private const val serialVersionUID = -4597228673980239217L
    }
}
