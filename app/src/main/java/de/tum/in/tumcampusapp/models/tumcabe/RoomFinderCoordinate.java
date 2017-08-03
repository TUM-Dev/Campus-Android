package de.tum.in.tumcampusapp.models.tumcabe;

public class RoomFinderCoordinate {
    String utm_zone;
    String utm_easting;
    String utm_northing;

    public RoomFinderCoordinate(String utm_zone, String utm_easting, String utm_northing) {
        this.utm_zone = utm_zone;
        this.utm_easting = utm_easting;
        this.utm_northing = utm_northing;
    }

    public String getUtm_zone() {
        return utm_zone;
    }

    public String getUtm_easting() {
        return utm_easting;
    }

    public String getUtm_northing() {
        return utm_northing;
    }
}
