package de.tum.`in`.tumcampusapp.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils

class GeoFencingRegistrationWorker(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {

    private lateinit var locationClient: GeofencingClient

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        locationClient = LocationServices.getGeofencingClient(applicationContext)

        Utils.log("Geo fencing service worker started …")
        if (!isLocationPermissionGranted()) {
            return Result.failure()
        }
        // get the data to create the Geofence request
        val id = inputData.getString(Const.ADD_GEOFENCE_ID) ?: return Result.failure()
        val latitude = inputData.getDouble(Const.ADD_GEOFENCE_LAT, 48.137430)
        val longitude = inputData.getDouble(Const.ADD_GEOFENCE_LON, 11.575490)
        val range = inputData.getFloat(Const.ADD_GEOFENCE_RANGE, 50_000f)

        // build request
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

        val geofenceIntent = Intent(applicationContext, GeofencingUpdateReceiver::class.java)
        val geofencePendingIntent = PendingIntent.getBroadcast(
                applicationContext, 0, geofenceIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        locationClient.addGeofences(geofencingRequest, geofencePendingIntent)
        Utils.log("Registered new Geofence")
        // Indicate whether the work finished successfully with the Result*/
        return Result.success()
    }

    companion object {

        /**
         * Helper method for creating an intent containing a geofencing request
         */
        fun buildGeofence(id: String, latitude: Double, longitude: Double, range: Float): WorkRequest {
            val data = Data.Builder()
                    .putString(Const.ADD_GEOFENCE_ID, id)
                    .putDouble(Const.ADD_GEOFENCE_LAT, latitude)
                    .putDouble(Const.ADD_GEOFENCE_LON, longitude)
                    .putFloat(Const.ADD_GEOFENCE_RANGE, range)
                    .build()

            return OneTimeWorkRequestBuilder<GeoFencingRegistrationWorker>()
                    .setInputData(data)
                    .build()
        }
    }
}
