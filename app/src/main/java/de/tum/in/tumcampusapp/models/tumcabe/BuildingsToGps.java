package de.tum.in.tumcampusapp.models.tumcabe;

public class BuildingsToGps {
    String id;
    String latitude;
    String longitude;

    public BuildingsToGps(String id, String latitude, String longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
