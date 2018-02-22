package de.tum.in.tumcampusapp.component.other.wifimeasurement;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import java.util.Calendar;

import de.tum.in.tumcampusapp.component.other.wifimeasurement.model.WifiMeasurement;

/**
 * This class defines the minimum requirements for a wifi measurement to be stored to the local database,
 * which later gets synced with the remote database to generate the wifi heatmap. Only if the measurement
 * is not older than MAX_MILLISECONDS_PASSED when the location is found it will get stored. Additionally
 * the measurement location data needs to fulfill the MINIMUM_ACCURACY_IN_METERS.
 */

public class WifiMeasurementLocationListener implements LocationListener {
    private final Context context;
    private final WifiMeasurement wifiMeasurement;
    //MAX_MILLISECONDS_PASSED defines the maximum time we wait until a location was found before throwing
    //the measurement away
    private static final int MAX_MILLISECONDS_PASSED = 3000;
    //MINIMUM_ACCURACY_IN_METERS defines the minimum location accuracy. If the location is less
    //accurate the measurement is thrown away
    private static final int MINIMUM_ACCURACY_IN_METERS = 25;
    //The Calendar-time we filled the measurement with wifi information, meaning that location information
    //is not yet generated
    private final long measurementTakenInMillis;

    public WifiMeasurementLocationListener(Context context, WifiMeasurement wifiMeasurement, long measurementTakenInMillis) {
        this.context = context;
        this.wifiMeasurement = wifiMeasurement;
        this.measurementTakenInMillis = measurementTakenInMillis;
    }

    /**
     * This method modifies the measurement it's responsible for by adding location data to it.
     * If it fulfills the minimum requirements to be stored, it is then saved to the local database.
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        //Since onLocationChanged can take time, this condition decides, whether the found location is
        //already too old (MAX_MILLISECONDS_PASSED) for being used for the measurement data (dBm etc...),
        //which were originally taken at measurementTakenInMillis
        boolean locationFixTookTooLong = Calendar.getInstance()
                                                 .getTimeInMillis() - measurementTakenInMillis > MAX_MILLISECONDS_PASSED;
        if (location.getAccuracy() >= MINIMUM_ACCURACY_IN_METERS || locationFixTookTooLong) {
            return;
        }
        WifiMeasurementManager wifiMeasurementManager = new WifiMeasurementManager(context);
        wifiMeasurement.setAccuracyInMeters(location.getAccuracy());
        wifiMeasurement.setLatitude(location.getLatitude());
        wifiMeasurement.setLongitude(location.getLongitude());
        wifiMeasurementManager.insertWifiMeasurement(wifiMeasurement);
    }

    @Override
    public void onStatusChanged(String status, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String status) {
    }

    @Override
    public void onProviderDisabled(String status) {
    }
}
