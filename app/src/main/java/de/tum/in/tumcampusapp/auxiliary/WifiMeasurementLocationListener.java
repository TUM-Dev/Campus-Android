package de.tum.in.tumcampusapp.auxiliary;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;
import java.util.Calendar;
import de.tum.in.tumcampusapp.managers.WifiMeasurementManager;
import de.tum.in.tumcampusapp.models.tumcabe.WifiMeasurement;

/**
 * This class defines the minimum requirements for a wifi measurement to be stored to the local database,
 * which later gets synced with the remote database to generate the wifi heatmap. Only if the measurement
 * is not older than MAX_MILLISECONDS_PASSED when the location is found it will get stored. Additionally
 * the measurement location data needs to fulfill the MINIMUM_ACCURACY_IN_METERS.
 */

public class WifiMeasurementLocationListener implements LocationListener{
    private Context context;
    private WifiMeasurement wifiMeasurement;
    private static final int MAX_MILLISECONDS_PASSED = 3000;
    private static final int MINIMUM_ACCURACY_IN_METERS = 25;
    private final long measurementTakenInMillis;
    public WifiMeasurementLocationListener(Context context, WifiMeasurement wifiMeasurement, long measurementTakenInMillis){
        this.context = context;
        this.wifiMeasurement = wifiMeasurement;
        this.measurementTakenInMillis = measurementTakenInMillis;
    }

    /**
     * This method modifies the measurement it's responsible for by adding location data to it.
     * If it fulfills the minimum requirements to be stored, it is then saved to the local database.
     * @param location
     */
        @Override
        public void onLocationChanged(Location location) {
            String date = Utils.getDateString(Calendar.getInstance().getTime());
            WifiMeasurementManager wifiMeasurementManager = new WifiMeasurementManager(context);
            if (Calendar.getInstance().getTimeInMillis() - measurementTakenInMillis > MAX_MILLISECONDS_PASSED) {
                Utils.log("Wifi Measurement thrown away, location stale!");
                return;
            }
            if (location.getAccuracy() < MINIMUM_ACCURACY_IN_METERS){
                wifiMeasurement.setAccuracyInMeters(location.getAccuracy());
                wifiMeasurement.setLatitude(location.getLatitude());
                wifiMeasurement.setLongitude(location.getLongitude());
                wifiMeasurementManager.insertWifiMeasurement(wifiMeasurement);
            }
        }

        @Override
        public void onStatusChanged(String status, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String status) {}

        @Override
        public void onProviderDisabled(String status) {}
}
