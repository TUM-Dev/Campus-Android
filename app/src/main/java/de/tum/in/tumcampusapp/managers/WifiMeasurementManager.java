package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dao.WifiMeasurementDao;
import de.tum.in.tumcampusapp.models.tumcabe.TUMCabeStatus;
import de.tum.in.tumcampusapp.models.tumcabe.WifiMeasurement;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WifiMeasurementManager {
    public static final String WIFI_SCAN_MINIMUM_BATTERY_LEVEL = "WIFI_SCAN_MINIMUM_BATTERY_LEVEL";

    private final WifiMeasurementDao dao;
    private final Context mContext;

    public WifiMeasurementManager(Context context) {
        mContext = context;
        dao = TcaDb.getInstance(context)
                   .wifiMeasurementDao();
    }

    public void insertWifiMeasurement(WifiMeasurement wifiMeasurement) {
        dao.insert(wifiMeasurement);
    }

    /**
     * Calls the api-interface for storing the measurement data
     *
     * @param wifiMeasurements
     * @throws IOException
     */
    private void sendMeasurementsToRemote(List<WifiMeasurement> wifiMeasurements) throws IOException {
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
     * This method iterates through all local database entries and tries to store them
     * to the server. If an error during network transmission occurs, it tries a maximum of
     * maxRetries times and waits waitTimeInMillis. It then purges the local database.
     *
     * @param maxRetries       The amount of maximum retries for sending a single measurement
     * @param waitTimeInMillis The wait time after a failed attempt to send a measurement to the server
     */
    public void uploadMeasurementsToRemote(int maxRetries, int waitTimeInMillis) {
        //Collect wifiMeasurements
        List<WifiMeasurement> wifiMeasurements = dao.getAll();

        if (wifiMeasurements.isEmpty()) {
            return;
        }

        //Try to send them to the server
        boolean successful = false;
        int currentAttempts = 0;
        while (currentAttempts < maxRetries && !successful) {
            try {
                sendMeasurementsToRemote(wifiMeasurements);
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

        //Only remove them if we were successful
        if (successful) {
            dao.cleanup();
        }
    }
}
