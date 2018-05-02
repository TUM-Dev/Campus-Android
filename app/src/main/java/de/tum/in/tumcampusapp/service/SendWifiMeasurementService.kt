package de.tum.`in`.tumcampusapp.service

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import de.tum.`in`.tumcampusapp.component.other.wifimeasurement.WifiMeasurementManager
import de.tum.`in`.tumcampusapp.utils.Const

/**
 * This service is getting used by StartSyncReceiver to send the accumulated WifiMeasurements
 * every X hours
 */
class SendWifiMeasurementService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        WifiMeasurementManager(this)
                .uploadMeasurementsToRemote(MAX_SEND_TRIES, TIME_BETWEEN_MILIS)
    }

    companion object {

        //Maximum retries for sending the wifi-measurement list to the server
        @JvmField val MAX_SEND_TRIES = 3
        //Time between retries for trying again
        @JvmField val TIME_BETWEEN_MILIS = 300

        @JvmStatic fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, SendWifiMeasurementService::class.java, Const.SEND_WIFI_SERVICE_JOB_ID, work)
        }

    }

}


