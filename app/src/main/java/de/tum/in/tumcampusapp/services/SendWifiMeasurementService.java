package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import de.tum.in.tumcampusapp.managers.WifiMeasurementManager;

/**
 * This service is getting used by StartSyncReceiver to send the accumulated WifiMeasurements
 * every X hours
 */
public class SendWifiMeasurementService extends JobIntentService {

    //Maximum retries for sending the wifi-measurement list to the server
    public static final int MAX_SEND_TRIES = 3;
    //Time between retries for trying again
    public static final int TIME_BETWEEN_MILIS = 300;
    static final int JOB_ID = 1003;
    private static final String SEND_WIFI_MEASUREMENT_SERVICE = "SendWifiMeasurementService";


    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SendWifiMeasurementService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        WifiMeasurementManager wm = new WifiMeasurementManager(this);
        wm.uploadMeasurementsToRemote(MAX_SEND_TRIES, TIME_BETWEEN_MILIS);
    }
}


