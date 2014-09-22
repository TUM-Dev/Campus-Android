package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "geo")
public class Geo {
    @SuppressWarnings("FieldCanBeLocal")
    @Element(required = false)
    private final String latitude = "0";

    @SuppressWarnings("FieldCanBeLocal")
    @Element(required = false)
    private final String longitude = "0";

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

}