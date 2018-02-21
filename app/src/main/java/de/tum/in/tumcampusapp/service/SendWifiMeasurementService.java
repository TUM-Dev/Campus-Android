package de.tum.in.tumcampusapp.service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import de.tum.in.tumcampusapp.component.other.wifimeasurement.WifiMeasurementManager;

import static de.tum.in.tumcampusapp.utils.Const.SEND_WIFI_SERVICE_JOB_ID;

/**
 * This service is getting used by StartSyncReceiver to send the accumulated WifiMeasurements
 * every X hours
 */
public class SendWifiMeasurementService extends JobIntentService {

    //Maximum retries for sending the wifi-measurement list to the server
    public static final int MAX_SEND_TRIES = 3;
    //Time between retries for trying again
    public static final int TIME_BETWEEN_MILIS = 300;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SendWifiMeasurementService.class, SEND_WIFI_SERVICE_JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        WifiMeasurementManager wm = new WifiMeasurementManager(this);
        wm.uploadMeasurementsToRemote(MAX_SEND_TRIES, TIME_BETWEEN_MILIS);
    }
}


