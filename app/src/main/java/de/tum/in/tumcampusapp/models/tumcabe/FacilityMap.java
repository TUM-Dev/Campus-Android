package de.tum.in.tumcampusapp.models.tumcabe;

import java.io.Serializable;

/**
 * Created by shayansiddiqui on 18.07.17.
 */

public class FacilityMap implements Serializable {
    private static final long serialVersionUID=1;
    private String mapString;
    private Double latitude;
    private Double longitude;

    public String getMapString() {
        return mapString;
    }

    public void setMapString(String mapString) {
        this.mapString = mapString;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
