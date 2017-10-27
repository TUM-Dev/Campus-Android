package de.tum.in.tumcampusapp.models.tumcabe;

public class WifiMeasurement {
    private final String date;
    private final String SSID;
    private final String BSSID;
    private final int dBm;
    private float accuracyInMeters;
    private double latitude;
    private double longitude;

    public WifiMeasurement(String date, String SSID, String BSSID, int dBm, float accuracyInMeters, double latitude, double longitude) {
        this.date = date;
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.dBm = dBm;
        this.accuracyInMeters = accuracyInMeters;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public String getSSID() {
        return SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getdBm() {
        return dBm;
    }

    public float getAccuracyInMeters() {
        return accuracyInMeters;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setAccuracyInMeters(float accuracyInMeters) {
        this.accuracyInMeters = accuracyInMeters;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "WifiMeasurement{" +
               "date='" + date + '\'' +
               ", SSID='" + SSID + '\'' +
               ", BSSID='" + BSSID + '\'' +
               ", dBm=" + dBm +
               ", accuracyInMeters=" + accuracyInMeters +
               ", latitude=" + latitude +
               ", longitude=" + longitude +
               '}';
    }
}
