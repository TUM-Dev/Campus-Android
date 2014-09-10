package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "geo")
public class Geo {
    @Element(required = false)
    private String latitude = "0";

    @Element(required = false)
    private String longitude = "0";

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

}