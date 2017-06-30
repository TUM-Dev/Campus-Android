package de.tum.in.tumcampusapp.managers;
import android.content.Context;
import android.database.Cursor;
import java.io.IOException;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumcabe.WifiMeasurement;

public class WifiMeasurementManager extends AbstractManager {
    public WifiMeasurementManager(Context context) {
        super(context);
        db.execSQL("CREATE TABLE IF NOT EXISTS wifi_measurement ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, date VARCHAR, SSID VARCHAR, "
                + "BSSID VARCHAR, dBm INTEGER, accuracyInMeters REAL, latitude REAL, longitude REAL)");
    }

    public void insertWifiMeasurement(WifiMeasurement wifiMeasurement){
        String params[] = new String[]{
        wifiMeasurement.getSSID(),
        wifiMeasurement.getBSSID(),
        String.valueOf(wifiMeasurement.getdBm()),
        String.valueOf(wifiMeasurement.getAccuracyInMeters()),
        String.valueOf(wifiMeasurement.getLatitude()),
        String.valueOf(wifiMeasurement.getLongitude())};
        db.execSQL("INSERT INTO wifi_measurement (date, SSID, BSSID, dBm, accuracyInMeters, latitude, longitude) VALUES (datetime('now'),?, ?, ?, ?, ?, ?)", params);
    }

    /**
     * Calls the api-interface for storing the measurement data
     * @param wifiMeasurement
     * @throws IOException
     */

    private void sendMeasurementToRemote(final WifiMeasurement wifiMeasurement) throws IOException {
        TUMCabeClient tumCabeClient = TUMCabeClient.getInstance(mContext);
        tumCabeClient.createMeasurement(wifiMeasurement);
    }

    /**
     * If cursor is at a valid position in the database, the method constructs a WifiMeasurement
     * from the row at atPosition.
     * @param atPosition
     * @return
     */
    private WifiMeasurement getWifiMeasurement(Cursor atPosition) {
        if(atPosition.isAfterLast()){
            return null;
        }
        String date = atPosition.getString(1);
        String SSID = atPosition.getString(2);
        String BSSID = atPosition.getString(3);
        int dBm = atPosition.getInt(4);
        float accuracy = atPosition.getInt(5);
        double latitude = atPosition.getDouble(6);
        double longitude = atPosition.getDouble(7);
        return new WifiMeasurement(date,SSID,BSSID,dBm, accuracy, latitude,longitude);
    }

    public void printLastLine(){
        Cursor c = db.rawQuery("SELECT * FROM wifi_measurement ORDER BY id DESC LIMIT 1;", null);
        if(c.moveToNext()) {
            String message = "";
            for (int i = 0; i < c.getColumnCount(); i++) {
                message += " " + c.getString(i);
            }
            if (!message.isEmpty()) {
                Utils.log("WifiMeasurement" + message);
            }
        }
    }

    public void printToLog(){
        Cursor c = db.rawQuery("SELECT * from wifi_measurement",null);
        while (c.moveToNext()){
            String date = c.getString(1);
            String SSID = c.getString(2);
            String level = c.getString(4);
            Utils.log("MEASUREMENT: "+date+" "+SSID+" "+level);
        }
    }

    /**
     * This method iterates through all local database entries and tries to store them
     * to the server. If an error during network transmission occurs, it tries a maximum of
     * maxRetries times and waits waitTimeInMillis. It then purges the local database.
     * @param maxRetries
     * The amount of maximum retries for sending a single measurement
     * @param waitTimeInMillis
     * The wait time after a failed attempt to send a measurement to the server
     */
    public void uploadMeasurementsToRemote(int maxRetries, int waitTimeInMillis) {
        Cursor wifiMeasurementIterator = db.rawQuery("SELECT * FROM wifi_measurement;", null);
        while(wifiMeasurementIterator.moveToNext()){
            Utils.log("Attempting to send wifiMeasurement NR: "+wifiMeasurementIterator.getPosition());
            boolean successful = false;
            int currentAttempts = 0;
            while (currentAttempts < maxRetries && !successful) {
                try {
                    WifiMeasurement wifiMeasurement = getWifiMeasurement(wifiMeasurementIterator);
                    if (wifiMeasurement != null){
                        sendMeasurementToRemote(wifiMeasurement);
                    }
                    successful = true;
                } catch (IOException e) {
                    Utils.log(e);
                    try{
                        Thread.sleep(waitTimeInMillis);
                    }
                    catch(InterruptedException ie){
                        Utils.log(ie);
                    }
                }
                currentAttempts++;
            }
        }
        db.execSQL("DELETE FROM wifi_measurement;");
    }
}
