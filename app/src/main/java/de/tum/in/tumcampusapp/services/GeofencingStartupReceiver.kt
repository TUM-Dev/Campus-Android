package de.tum.`in`.tumcampusapp.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.auxiliary.Const.DISTANCE_IN_METER
import de.tum.`in`.tumcampusapp.auxiliary.Const.MUNICH_GEOFENCE
import de.tum.`in`.tumcampusapp.auxiliary.Utils


class GeofencingStartupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (!isValidIntent(intent)) {
            return
        }
        Utils.log("Restarting geofencing due to " + intent?.action)
        if (context != null) {
            val geofencingIntent = GeofencingRegistrationService.buildGeofence(context, MUNICH_GEOFENCE,
                    48.137430, 11.575490, DISTANCE_IN_METER)
            GeofencingRegistrationService.startGeofencing(context, geofencingIntent)
        }
    }

    private fun isValidIntent(intent: Intent?): Boolean {
        return intent != null && (intent.action == "android.intent.action.BOOT_COMPLETED" ||
                intent.action == "android.location.MODE_CHANGED" ||
                intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
                intent.action == "android.location.PROVIDERS_CHANGED")
    }
}