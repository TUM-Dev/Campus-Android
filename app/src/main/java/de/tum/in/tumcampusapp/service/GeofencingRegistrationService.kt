package de.tum.`in`.tumcampusapp.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.JobIntentService
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Const.ADD_GEOFENCE_EXTRA
import de.tum.`in`.tumcampusapp.utils.Const.GEOFENCING_SERVICE_JOB_ID
import de.tum.`in`.tumcampusapp.utils.Utils


/**
 * Service that receives Geofencing requests and registers them.
 */
class GeofencingRegistrationService : JobIntentService() {

    private var locationClient: GeofencingClient? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getGeofencingClient(baseContext)
        Utils.log("Service started")
    }

    @SuppressLint("MissingPermission")
    override fun onHandleWork(intent: Intent) {
        if (!isLocationPermissionGranted()) {
            return
        }

        val request = intent.getParcelableExtra<GeofencingRequest>(Const.ADD_GEOFENCE_EXTRA) ?: return
        val geofenceIntent = Intent(this, GeofencingUpdateReceiver::class.java)
        val geofencePendingIntent = PendingIntent.getBroadcast(
                this, 0, geofenceIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        locationClient?.addGeofences(request, geofencePendingIntent)
        Utils.log("Registered new Geofence")
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    companion object {

        @JvmStatic fun startGeofencing(context: Context, work: Intent) {
            enqueueWork(context, GeofencingRegistrationService::class.java, GEOFENCING_SERVICE_JOB_ID, work)
        }

        /**
         * Helper method for creating an intent containing a geofencing request
         */
        fun buildGeofence(context: Context, id: String, latitude: Double, longitude: Double, range: Float): Intent {
            val intent = Intent(context, GeofencingRegistrationService::class.java)
            val geofence = Geofence.Builder()
                    .setRequestId(id)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setCircularRegion(latitude, longitude, range)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build()
            val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
                    .addGeofences(arrayListOf(geofence))
                    .build()
            return intent.putExtra(ADD_GEOFENCE_EXTRA, geofencingRequest)
        }

    }

}