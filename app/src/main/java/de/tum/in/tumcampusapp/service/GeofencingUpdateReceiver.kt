package de.tum.`in`.tumcampusapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils

/**
 * Receiver for Geofencing updates regarding munich.
 *
 * This class will disable or enable the background mode depending on whether the user
 * entered or left munich.
 */
class GeofencingUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Utils.logwithTag(TAG, "Received event")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Utils.logwithTag(TAG, "Geofencing event contained errors.")
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Utils.setSetting(context, Const.BACKGROUND_MODE, true)
            StartSyncReceiver.startBackground(context)
            Utils.logwithTag(TAG, "Geofencing detected user entering munich, " +
                    "enabling Auto updates")
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Utils.setSetting(context, Const.BACKGROUND_MODE, false)
            StartSyncReceiver.cancelBackground()
            Utils.logwithTag(TAG, "Geofencing detected user leaving munich, " +
                    "disabling Auto updates")
        }
    }

    companion object {
        const val TAG = "GeofencingUpdateReceiver"
    }
}