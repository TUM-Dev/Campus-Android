package de.tum.`in`.tumcampusapp.models.gcm

import java.io.Serializable

data class GCMNotificationLocation(var location: Int = 0, var name: String = "", var lon: Double = .0, var lat: Double = .0, var radius: Int = 0) :
        Serializable {
    fun getRadius(): Double {
        return radius.toDouble()
    }

    companion object {
        private const val serialVersionUID = 3617955892672212188L
    }
}
