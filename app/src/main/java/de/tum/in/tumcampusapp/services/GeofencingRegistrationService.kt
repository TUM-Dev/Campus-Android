package de.tum.`in`.tumcampusapp.services

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
import de.tum.`in`.tumcampusapp.auxiliary.Const.GEOFENCING_SERVICE_JOB_ID
import de.tum.`in`.tumcampusapp.auxiliary.Utils


/**
 * Service that receives Geofencing requests and registers them.
 */
class GeofencingRegistrationService : JobIntentService() {

    private lateinit var mGeofencePendingIntent: PendingIntent
    private lateinit var locationClient: GeofencingClient

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getGeofencingClient(baseContext)
        val intent = Intent(this, GeofencingUpdateReceiver::class.java)
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Utils.logwithTag(TAG, "Service started")
    }

    @SuppressLint("MissingPermission")
    override fun onHandleWork(intent: Intent) {
        if (!isLocationPermissionGranted())
            return
        val mRequest = intent.getParcelableExtra<GeofencingRequest>(ADD_GEOFENCE)
        if (mRequest != null) {
            locationClient.addGeofences(
                    mRequest,
                    mGeofencePendingIntent
            )
            Utils.logwithTag(TAG, "Registered new Geofence")
        }

    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    companion object {
        const val TAG = "GeofencingRegistrationService"
        const val ADD_GEOFENCE = "AddGeofence"


        @JvmStatic
        fun startGeofencing(context: Context, work: Intent) {
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
            val builder = GeofencingRequest.Builder()
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            builder.addGeofences(arrayListOf(geofence))
            return intent.putExtra(ADD_GEOFENCE, builder.build())
        }
    }
}