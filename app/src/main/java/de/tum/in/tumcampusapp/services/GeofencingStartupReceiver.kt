package de.tum.`in`.tumcampusapp.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.auxiliary.Utils


class GeofencingStartupReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        if (!isValidIntent(p1))
            return

        Utils.logwithTag(TAG, "Restarting geofencing due to " + p1?.action)
        if (p0 != null) {
            val intent = GeofencingRegistrationService.buildGeofence(p0, MUNICH_GEOFENCE,
                    48.137430, 11.575490, DISTANCE_IN_METER)
            GeofencingRegistrationService.startGeofencing(p0, intent)
        }
    }

    private fun isValidIntent(intent: Intent?): Boolean {
        return intent != null && (intent.action == "android.intent.action.BOOT_COMPLETED" ||
                intent.action == "android.location.MODE_CHANGED" ||
                intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
                intent.action == "android.location.PROVIDERS_CHANGED")
    }

    companion object {
        const val TAG = "GeofencingStartupReceiver"
        const val DISTANCE_IN_METER = 50 * 1000f
        const val MUNICH_GEOFENCE = "geofence_munich_id"
    }
}