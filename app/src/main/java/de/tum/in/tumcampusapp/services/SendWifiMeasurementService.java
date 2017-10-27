package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.content.Intent;

import de.tum.in.tumcampusapp.managers.WifiMeasurementManager;

/**
 * This service is getting used by StartSyncReceiver to send the accumulated WifiMeasurements
 * every X hours
 */
public class SendWifiMeasurementService extends IntentService {

    //Maximum retries for sending the wifi-measurement list to the server
    public static final int MAX_SEND_TRIES = 3;
    //Time between retries for trying again
    public static final int TIME_BETWEEN_MILIS = 300;
    private static final String SEND_WIFI_MEASUREMENT_SERVICE = "SendWifiMeasurementService";

    public SendWifiMeasurementService() {
        super(SEND_WIFI_MEASUREMENT_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WifiMeasurementManager wm = new WifiMeasurementManager(this);
        wm.uploadMeasurementsToRemote(MAX_SEND_TRIES, TIME_BETWEEN_MILIS);
    }
}


