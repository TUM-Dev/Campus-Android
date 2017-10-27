package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumcabe.TUMCabeStatus;
import de.tum.in.tumcampusapp.models.tumcabe.WifiMeasurement;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WifiMeasurementManager extends AbstractManager {
    public static final String WIFI_SCANS_ALLOWED = "WIFI_SCANS_ALLOWED";
    public static final String WIFI_SCAN_MINIMUM_BATTERY_LEVEL = "WIFI_SCAN_MINIMUM_BATTERY_LEVEL";

    public WifiMeasurementManager(Context context) {
        super(context);
        db.execSQL("CREATE TABLE IF NOT EXISTS wifi_measurement ("
                   + "id INTEGER PRIMARY KEY AUTOINCREMENT, date VARCHAR, SSID VARCHAR, "
                   + "BSSID VARCHAR, dBm INTEGER, accuracyInMeters REAL, latitude REAL, longitude REAL)");
    }

    public void insertWifiMeasurement(WifiMeasurement wifiMeasurement) {
        String[] params = {wifiMeasurement.getSSID(),
                           wifiMeasurement.getBSSID(),
                           String.valueOf(wifiMeasurement.getdBm()),
                           String.valueOf(wifiMeasurement.getAccuracyInMeters()),
                           String.valueOf(wifiMeasurement.getLatitude()),
                           String.valueOf(wifiMeasurement.getLongitude())
        };
        db.execSQL("INSERT INTO wifi_measurement (date, SSID, BSSID, dBm, accuracyInMeters, latitude, longitude) VALUES (datetime('now'),?, ?, ?, ?, ?, ?)", params);
    }

    /**
     * Calls the api-interface for storing the measurement data
     *
     * @param wifiMeasurements
     * @throws IOException
     */

    private void sendMeasurementsToRemote(final WifiMeasurement[] wifiMeasurements) throws IOException {
        TUMCabeClient tumCabeClient = TUMCabeClient.getInstance(mContext);
        tumCabeClient.createMeasurements(wifiMeasurements, new Callback<TUMCabeStatus>() {
            @Override
            public void onResponse(@NonNull Call<TUMCabeStatus> call, @NonNull Response<TUMCabeStatus> response) {
                Utils.log("WifiMeasurements successfully sent to the server! " + response);
            }

            @Override
            public void onFailure(@NonNull Call<TUMCabeStatus> call, @NonNull Throwable throwable) {
                Utils.log("WifiMeasurements weren't sent to the server! " + throwable.getMessage());
            }
        });
    }

    /**
     * If cursor is at a valid position in the database, the method constructs a WifiMeasurement
     * from the row at atPosition.
     *
     * @param atPosition
     * @return
     */
    private WifiMeasurement getWifiMeasurement(Cursor atPosition) {
        if (atPosition.isAfterLast()) {
            return null;
        }
        String date = atPosition.getString(1);
        String SSID = atPosition.getString(2);
        String BSSID = atPosition.getString(3);
        int dBm = atPosition.getInt(4);
        float accuracy = atPosition.getInt(5);
        double latitude = atPosition.getDouble(6);
        double longitude = atPosition.getDouble(7);
        return new WifiMeasurement(date, SSID, BSSID, dBm, accuracy, latitude, longitude);
    }

    /**
     * This method iterates through all local database entries and tries to store them
     * to the server. If an error during network transmission occurs, it tries a maximum of
     * maxRetries times and waits waitTimeInMillis. It then purges the local database.
     *
     * @param maxRetries       The amount of maximum retries for sending a single measurement
     * @param waitTimeInMillis The wait time after a failed attempt to send a measurement to the server
     */
    public void uploadMeasurementsToRemote(int maxRetries, int waitTimeInMillis) {
        //Collect wifiMeasurements
        ArrayList<WifiMeasurement> wifiMeasurements;
        WifiMeasurement[] measurementsAsArray;
        boolean successful;
        int currentAttempts;
        try (Cursor wifiMeasurementIterator = db.rawQuery("SELECT * FROM wifi_measurement;", null)) {
            wifiMeasurements = new ArrayList<>();
            while (wifiMeasurementIterator.moveToNext()) {
                wifiMeasurements.add(getWifiMeasurement(wifiMeasurementIterator));
            }
        }
        if (wifiMeasurements.isEmpty()) {
            Utils.log("WifiMeasurements Empty");
            return;
        }

        measurementsAsArray = new WifiMeasurement[wifiMeasurements.size()];
        measurementsAsArray = wifiMeasurements.toArray(measurementsAsArray);

        //Try to send them to the server
        successful = false;
        currentAttempts = 0;
        while (currentAttempts < maxRetries && !successful) {
            try {
                sendMeasurementsToRemote(measurementsAsArray);
                successful = true;
            } catch (IOException e) {
                Utils.log(e);
                try {
                    Thread.sleep(waitTimeInMillis);
                } catch (InterruptedException ie) {
                    Utils.log(ie);
                }
            }
            currentAttempts++;
        }
        db.execSQL("DELETE FROM wifi_measurement;");
    }
}
