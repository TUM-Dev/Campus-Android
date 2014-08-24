package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "geo")
public class Geo {

    public String getLatitude() {
        String latitude = "0";
        return latitude;
	}

	public String getLongitude() {
        String longitude = "0";
        return longitude;
	}

}