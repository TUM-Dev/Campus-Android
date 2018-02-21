package de.tum.`in`.tumcampusapp.models.gcm

import java.io.Serializable

/**
 * Used for parsing the GCM json payload for the 'update' type
 */
data class GCMUpdate(var packageVersion: Int = 0,
                     var sdkVersion: Int = 0,
                     var releaseDate: String = "",
                     var lowestVersion: Int = 0) : Serializable {
    companion object {
        private const val serialVersionUID = -4597228673980239217L
    }
}
